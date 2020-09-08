package main.com.skillbox.ru.developerspublics.service;


import main.com.skillbox.ru.developerspublics.api.request.RequestApiModeration;
import main.com.skillbox.ru.developerspublics.api.request.RequestPostPutApiPost;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.*;
import main.com.skillbox.ru.developerspublics.model.repository.*;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;


@Service
public class PostService {
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private GlobalSettingService globalSettingService;
    @Autowired
    private PostCommentService postCommentService;
    @Autowired
    private PostVoteService postVoteService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TagToPostService tagToPostService;
    @Autowired
    private UserService userService;

    //размер анонса
    @Value("${announce.size}")
    private int announceSize;


    public Post getInitPostById(int id) {
        Optional<Post> optionalPost = postsRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            initPost(post);
            return post;
        }
        return null;
    }


    public Post getPostById(int id) {
        if (postsRepository.findById(id).isPresent()) return postsRepository.findById(id).get();
        return null;
    }


    public Post getPostByTitle(String title) {
        return postsRepository.findByTitle(title);
    }


    public List<Post> getActivePosts() {
        List<Post> activePosts = new ArrayList<>();
        for (Post post : postsRepository.findByIsActiveAndModerationStatus(1, ModerationStatuses.ACCEPTED.toString())) {
            if (post.getTimestamp() <= (System.currentTimeMillis() / 1000)) {
                initPost(post);
                activePosts.add(post);
            }
        }
        return activePosts;
    }


    public List<Post> getPostsByUserId(int userId) {
        return postsRepository.findByUserId(userId);
    }


    public List<Post> getPostsByModerationStatus(String moderationStatus) {
        return postsRepository.findByModerationStatus(moderationStatus);
    }


    public boolean isPostActive(Post post) {
        //проверяем выполнение сразу 3х условий
        //1 - стоит галочка "пост активен"
        //2 - проверяем статус -> д.б. ACCEPTED
        //3 - проверяем дату публикации -> д.б. не в будующем
        return post.getIsActive() == 1 &&
                post.getModerationStatus().equals(ModerationStatuses.ACCEPTED.toString())
                && post.getTimestamp() <= (System.currentTimeMillis() / 1000);
    }


    public List<Post> getPosts() {
        return new ArrayList<>(postsRepository.findAll());
    }


    public void initPost(Post post) {
        if (post.getModeratorId() != null) {
            post.setModeratorPost(userService.getUserById(post.getModeratorId()));
        }

        post.setUserPost(userService.getUserById(post.getUserId()));

        post.setPostVotes(postVoteService.getPostVotesByPostId(post.getId()));

        post.setPostComments(postCommentService.getPostCommentsByPostId(post.getId()));

        post.setTagToPosts(tagToPostService.getTagToPostsByPostId(post.getId()));
    }


    public List<PostResponse> responsePosts(List<Post> posts, int offset, int limit, String mode) {
        List<Post> responsePosts = new ArrayList<>();
        //сортируем -> обрезаем -> переносим в список ответа
        sortedByMode(posts.stream(), mode)
                .skip(offset).limit(limit)
                .forEach(responsePosts::add);

        List<PostResponse> list = new ArrayList<>();
        //приводим к нужному виду
        for (Post post : responsePosts) {
            list.add(postToJSON(post));
        }
        return list;
    }


    public PostResponse postToJSON(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTimestamp(),
                new UserIdNameResponse(post.getUserPost().getId(), post.getUserPost().getName()),
                post.getTitle(),
                getAnnounce(post),
                getLikesDislikesCount(post,1),
                getLikesDislikesCount(post,-1),
                getCommentsCount(post),
                post.getViewCount()
        );
    }


    public PostByIdResponse postByIdToJSON(Post post) {
        return new PostByIdResponse(
                post.getId(),
                post.getTimestamp(),
                new UserIdNameResponse(post.getUserPost().getId(), post.getUserPost().getName()),
                post.getTitle(),
                post.getText(),
                post.getIsActive() == 1,
                getLikesDislikesCount(post,1),
                getLikesDislikesCount(post,-1),
                getCommentsCount(post),
                post.getViewCount(),
                postCommentService.getPostCommentResponseList(post.getPostComments()),
                getPostTagsList(post)
        );
    }


    public boolean isPostReadyToEditByModerator(Post post) {
        //не проверяем статус модерации
        return post.getIsActive() == 1 &&
                post.getTimestamp() <= (System.currentTimeMillis() / 1000);
    }


    public String getAnnounce(Post post) {
        String text = Jsoup.parse(post.getText()).text();
        return ((text.length() > announceSize) ? text.substring(0, announceSize) : text);
    }


    public int getLikesDislikesCount(Post post, int value) {
        //value = +1 -> like
        //value = -1 -> dislike
        int count = 0;
        for (PostVote postVote : post.getPostVotes()) {
            if (postVote.getValue() == value) {
                count++;
            }
        }
        return count;
    }


    public int getCommentsCount(Post post) {
        return post.getPostComments() == null ? 0 : post.getPostComments().size();
    }


    private List<String> getPostTagsList(Post post) {
        List<String> list = new ArrayList<>();
        for (TagToPost tagToPost : post.getTagToPosts()) {
            list.add(tagService.getTagById(tagToPost.getTagId()).getName());
        }
        return list;
    }


    private Stream<Post> sortedByMode(Stream<Post> stream, String mode) {
        if (mode.equals("")) {
            mode = "recent";
        }
        switch (mode){
            case "recent": return stream.sorted(Comparator.comparing(Post::getTime).reversed());

            case "popular": return stream.sorted(Comparator.comparing(e -> getCommentsCount((Post) e)).reversed());

            case "best": return stream.sorted(Comparator.comparing(e -> getLikesDislikesCount((Post) e,1)).reversed());

            case "early": return stream.sorted(Comparator.comparing(Post::getTime));
        }
        return stream;
    }


    public void savePost(long timestamp, int isActive, String title, String text, int userId, List<String> tagsNames) {
        //создаем новый
        Post post = new Post();
        //заполняем обязательные поля
        post.setTime(timestamp);
        post.setIsActive(isActive);
        post.setTitle(title);
        post.setText(text);
        post.setUserId(userId);
        //проверяем настройку премодерации:
        // - YES -> NEW
        // - NO  -> ACCEPTED
        if (globalSettingService.findGlobalSettingByCode(GlobalSettingsCodes.POST_PREMODERATION.toString()).getValue()
                .equals(GlobalSettingsValues.YES.toString())) {
            post.setModerationStatus(ModerationStatuses.NEW.getStatus());
        }
        else post.setModerationStatus(ModerationStatuses.ACCEPTED.toString());

        post.setViewCount(0);
        //отправляем в репозиторий
        postsRepository.save(post);

        //сохраним тэги и привяжем их к посту
        for (String tagName : tagsNames) {
            tagService.saveTag(tagName, post.getId());
        }
    }


    public void editPost(int id, long timestamp, int isActive, String title, String text, int userId,
                         List<String> tagsNames) {
        //находим в базе
        Post post = getPostById(id);
        //заполняем обязательные поля
        post.setTime(timestamp);
        post.setIsActive(isActive);
        post.setTitle(title);
        post.setText(text);

        //редактирует user -> ModerationStatuses.NEW
        //редактирует moderator -> ModerationStatus не меняем!
        if (userService.getUserById(userId).getIsModerator() == 0) {
            post.setModerationStatus(ModerationStatuses.NEW.getStatus());
        }

        //отправляем в репозиторий
        postsRepository.save(post); //TODO ограничить размер текста!!!

        //сохраним тэги и привяжем их к посту
        for (String tagName : tagsNames) {
            tagService.saveTag(tagName, post.getId());
        }
    }


    public void incrementViewCount(Post post) {
        post.setViewCount(post.getViewCount() + 1);
        postsRepository.save(post);
    }


    public boolean setModerationStatus(int postId, String status, int moderatorId) {
        Post post = getPostById(postId);
        if (post == null) return false;
        post.setModerationStatus(status);
        post.setModeratorId(moderatorId);
        postsRepository.save(post);
        return true;
    }


    public ResponseEntity<?> postApiPost(RequestPostPutApiPost requestBody) {
        return postPutApiPost(requestBody, null);
    }


    public ResponseEntity<?> putApiPost(RequestPostPutApiPost requestBody, int postId) {
        return postPutApiPost(requestBody, postId);
    }


    private ResponseEntity<?> postPutApiPost(RequestPostPutApiPost requestBody, Integer postId) {
        //актуализируем время
        long timestamp = requestBody.getTimestamp();
        if (timestamp < System.currentTimeMillis()) {
            timestamp = System.currentTimeMillis();
        }

        //заголовок не короче 3х символов
        String title = requestBody.getTitle();
        if (title.length() < 3)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(
                            ErrorsResponse.builder().title("Заголовок не установлен").build()
                    ));

        //текст не короче 50ти символов
        String text = requestBody.getText();
        if (text.length() < 50)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(
                            ErrorsResponse.builder().text("Текст публикации слишком короткий").build()
                    ));


        //ошибок нет - добавляем пост
        int userId = userService
                .findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName())
                .getId();
        int isActive = requestBody.getActive();
        List<String> tagsNames = requestBody.getTags();

        //POST /api/post  -> postId = null -> создаем новый
        //PUT /api/post/{ID}  -> postId != null -> изменяем
        if (postId == null) {
            savePost(timestamp, isActive, title, text, userId, tagsNames);
        }
        else editPost(postId, timestamp, isActive, title, text, userId, tagsNames);

        //возвращаем ответ
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultResponse(true));
    }


    public ResponseEntity<?> getApiPost(int offset, int limit, String mode) {
        //запоминаем активные посты
        List<Post> posts = getActivePosts();

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                responsePosts(posts, offset, limit, mode)
        ));
    }


    public ResponseEntity<?> getApiPostSearch(int offset, int limit, String query) {
        //создадим список активных постов posts
        List<Post> posts = getActivePosts();

        //создадим список для найденных постов findPosts
        List<Post> findPosts = new ArrayList<>();

        //если запрос пустой, метод должен выводить все посты
        if (query.length() == 0) {
            findPosts = posts;
        }
        else { //иначе ищем query в текстовых полях
            for (Post post : posts) {
                if (post.getTitle().contains(query) || post.getText().contains(query)) {
                    findPosts.add(post);
                }
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                findPosts.size(),
                responsePosts(findPosts, offset, limit, "")
        ));
    }


    public ResponseEntity<?> getApiPostId(int id) {
        //ищем нужный пост по id
        Post post = getInitPostById(id);

        //При успешном запросе увеличиваем количество просмотров поста на 1, кроме случаев:
        // - Если модератор авторизован, то не считаем его просмотры вообще
        // - Если автор авторизован, то не считаем просмотры своих же публикаций
        User user = userService.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        //если пост не найден - возвращаем 404
        if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        initPost(post);

        //смотрит не аноним
        if (user != null) {
            //автор смотрит без условий
            if (post.getUserId() == user.getId())
                return ResponseEntity.status(HttpStatus.OK).body(postByIdToJSON(post));

            //если модератор - проверяем дату и isActive
            if (user.getIsModerator() == 1 && isPostReadyToEditByModerator(post))
                return ResponseEntity.status(HttpStatus.OK).body(postByIdToJSON(post));
        }

        //если не автор и не модератор - проверяем активность и плюсуем просмотры
        if (isPostActive(post)) {
            incrementViewCount(post);
            return ResponseEntity.status(HttpStatus.OK).body(postByIdToJSON(post));
        }

        //посторонний пытается посмотреть не активный пост
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    public ResponseEntity<?> getApiPostByDate(int offset, int limit, String date) {
        ArrayList<Post> posts = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //перебираем активные посты
        for (Post post : getActivePosts()) {
            //ищем посты с заданной датой
            if (dateFormat.format(post.getTime()).equals(date)) {
                //результат запоминаем в posts
                posts.add(post);
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                responsePosts(posts, offset, limit, "")
        ));
    }


    public ResponseEntity<?> getApiPostByTag(int offset, int limit, String tagName) {
        ArrayList<Post> posts = new ArrayList<>();

        //получаем список тэг-пост для тега по имени тэга
        for (TagToPost tagToPost : tagToPostService.getTagToPostsByTagName(tagName)) {
            //находим пост по id
            Post post = getInitPostById(tagToPost.getPostId());
            //проверяем на активность
            if (isPostActive(post)) {
                //и запоминаем
                posts.add(post);
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                responsePosts(posts, offset, limit, "")
        ));
    }


    public ResponseEntity<?> getApiPostModeration(int offset, int limit, String status) {
        //выдергиваем из контекста пользователя
        User user = userService.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        status = status.toUpperCase();

        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : getPostsByModerationStatus(status)) {
            //отбираем активные посты с нужным статусом:
            //NEW - выводим все
            //ACCEPTED-DECLINED -> выводим только для текущего пользователя
            if (post.getIsActive() == 1) {
                initPost(post);
                if (status.equals(ModerationStatuses.NEW.toString())) {
                    posts.add(post);
                }
                else if (post.getModeratorId() != null) {
                    if (post.getModeratorId() == user.getId()) {
                        posts.add(post);
                    }
                }
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                responsePosts(posts, offset, limit, "")
        ));
    }


    public ResponseEntity<?> getApiPostMy(int offset, int limit, String status) {
        //выдергиваем из контекста пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(authentication.getName());

        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : getPostsByUserId(user.getId())) {
            initPost(post);
            //в зависимости от статуса добавляем нужные
            switch (status) {
                case "inactive" : if (post.getIsActive() == 0)
                    posts.add(post); break;
                case "pending" : if (post.getIsActive() == 1 &&
                        post.getModerationStatus().equals(ModerationStatuses.NEW.toString()))
                    posts.add(post); break;

                case "declined" : if (post.getIsActive() == 1 &&
                        post.getModerationStatus().equals(ModerationStatuses.DECLINED.toString()))
                    posts.add(post); break;

                case "published" : if (post.getIsActive() == 1 &&
                        post.getModerationStatus().equals(ModerationStatuses.ACCEPTED.toString()))
                    posts.add(post); break;
            }

        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(
                posts.size(),
                responsePosts(posts, offset, limit, "")
        ));
    }


    public ResponseEntity<?> postApiModeration(RequestApiModeration requestBody) {
        boolean hasErrors = false;
        String status = "";

        //получаем входные параметры
        int postId = requestBody.getPostId();
        String decision = requestBody.getDecision();
        //получаем пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User moderator = userService.findUserByLogin(authentication.getName());

        //переводим decision -> status
        if (decision.equals("accept")) status = ModerationStatuses.ACCEPTED.toString();
        else if (decision.equals("decline")) status = ModerationStatuses.DECLINED.toString();

        //проверяем на ошибки: не найден модератор, не правильный статус, не найден пост
        if (moderator == null || status.equals("")) hasErrors = true;
        else if (!setModerationStatus(postId, status, moderator.getId())) hasErrors = true;

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ResultResponse(!hasErrors));
    }


    public ResponseEntity<?> getApiCalendar(Integer year) {
        //init списка всех годов публикаций
        TreeSet<String> yearsWithPosts = new TreeSet<>();
        //init списка дата - кол-во постов
        TreeMap<String, Integer> dateCountPosts = new TreeMap<>(Comparator.reverseOrder());

        //подготовим два паттерна для форматирования дат
        DateFormat yearDF = new SimpleDateFormat("yyyy");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //если год не передан - возвращаем за текущий год
        if (year == 0) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        //для удобства переводим в String
        String stringYear = Integer.toString(year);

        //перебираем все активные посты
        for (Post post : getActivePosts()) {
            //запомним год публикации данного поста
            String postYear = yearDF.format(post.getTime());
            //занесем его в список
            yearsWithPosts.add(postYear);
            //если год публикации = выбранному году
            if (postYear.equals(stringYear)) {
                //сохраняем его в список дата-кол. постов
                String postDate = dateFormat.format(post.getTime());
                //проверяем есть ли в списке такая дата
                if (dateCountPosts.containsKey(postDate)) {
                    //если есть - инкрементируем кол-во
                    dateCountPosts.put(postDate, dateCountPosts.get(postDate) + 1);
                }   //иначе добавляем новую дату
                else dateCountPosts.put(postDate, 1);
            }
        }

        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiCalendarResponse(yearsWithPosts, dateCountPosts));
    }


    public ResponseEntity<?> getApiStatisticsMy() {
        User user = userService.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        int postsCount = 0;
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        long firstPublication = System.currentTimeMillis() / 1000;

        if (user != null) {
            for (Post post : getPostsByUserId(user.getId())) {
                if (isPostActive(post)) {

                    postsCount++;
                    viewsCount += post.getViewCount();

                    for (PostVote postVote : postVoteService.getPostVotesByPostId(post.getId())) {
                        if (postVote.getValue() == 1) likesCount++;
                        else dislikesCount++;
                    }

                    if (post.getTimestamp() < firstPublication) firstPublication = post.getTimestamp();
                }
            }
        }

        if (postsCount == 0) firstPublication = 0;

        return new ResponseEntity<>(new ApiStatisticsResponse(postsCount, likesCount, dislikesCount,
                viewsCount, firstPublication), HttpStatus.OK);
    }


    public ResponseEntity<?> getApiStatisticsAll() {
        //получим пользователя
        User user = userService.findUserByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        //получим настройку
        GlobalSetting globalSetting = globalSettingService.findGlobalSettingByCode(
                GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString());

        //if STATISTICS_IS_PUBLIC = NO & Auth=false -> HTTP.401
        if (globalSetting.getValue().equals(GlobalSettingsValues.NO.toString()) && user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        //init parameters
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        //timestamp of first publication
        long firstPublication = System.currentTimeMillis() / 1000;

        //перебираем все активные посты
        for (Post post : getActivePosts()) {
            //считаем общ. кол-во лайков
            likesCount += getLikesDislikesCount(post,1);
            //считаем общее кол-во дислайков
            dislikesCount += getLikesDislikesCount(post,-1);
            //считаем общее кол-во просмотров
            viewsCount += post.getViewCount();
            //ищем дату самого первого поста
            if (firstPublication > post.getTimestamp()) firstPublication = post.getTimestamp();
        }
        int postsCount = getPosts().size();

        //собираем ответ и возвращаем его
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiStatisticsResponse(postsCount, likesCount, dislikesCount, viewsCount,
                        (postsCount == 0) ? 0 : firstPublication));
    }
}