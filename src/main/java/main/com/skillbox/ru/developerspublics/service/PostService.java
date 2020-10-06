package main.com.skillbox.ru.developerspublics.service;


import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiModeration;
import main.com.skillbox.ru.developerspublics.api.request.RequestPostPutApiPost;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.enums.Decision;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.*;
import main.com.skillbox.ru.developerspublics.model.repository.*;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PostService {
    private final PostsRepository postsRepository;
    private final GlobalSettingService globalSettingService;
    private final PostCommentService postCommentService;
    private final PostVoteService postVoteService;
    private final TagService tagService;
    private final TagToPostService tagToPostService;
    private final UserService userService;

    //размер анонса
    @Value("${announce.size}")
    private int announceSize;


    @Autowired
    public PostService (PostsRepository postsRepository,
                        GlobalSettingService globalSettingService,
                        PostCommentService postCommentService,
                        PostVoteService postVoteService,
                        TagService tagService,
                        TagToPostService tagToPostService,
                        UserService userService) {
        this.postsRepository = postsRepository;
        this.globalSettingService = globalSettingService;
        this.postCommentService = postCommentService;
        this.postVoteService = postVoteService;
        this.tagService = tagService;
        this.tagToPostService = tagToPostService;
        this.userService = userService;
    }

//================================= GETTERS ============================================================================
    private Post getInitPostById(int id) {
        Optional<Post> optionalPost = postsRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            initPost(post);
            return post;
        }
        return null;
    }


    private Post getPostById(int id) {
        if (postsRepository.findById(id).isPresent()) return postsRepository.findById(id).get();
        return null;
    }


    private List<Post> getInitActivePosts() {
        List<Post> activePosts = new ArrayList<>();
        for (Post post : postsRepository.findByIsActiveAndModerationStatusAndTimeBefore(
                1, ModerationStatuses.ACCEPTED.toString(), new Date(System.currentTimeMillis()), null)) {
                initPost(post);
                activePosts.add(post);
        }
        return activePosts;
    }


    private List<Post> getActivePosts() {
        return postsRepository.findByIsActiveAndModerationStatusAndTimeBefore(
                1, ModerationStatuses.ACCEPTED.toString(), new Date(System.currentTimeMillis()), null);
    }


    private List<Post> getActivePosts(int offset, int limit, String mode) {
        if (mode.equals("")) mode = "recent";

        List<Post> posts = new ArrayList<>();
        switch (mode){
            case "recent": posts = postsRepository.findByIsActiveAndModerationStatusAndTimeBefore(
                                            1,
                                            ModerationStatuses.ACCEPTED.toString(),
                                            new Date(System.currentTimeMillis()),
                                            PageRequest.of(offset/limit, limit, Sort.by("time").descending()));
                            break;

            case "popular": posts = postsRepository.getPopularPosts(
                                            1,
                                            ModerationStatuses.ACCEPTED.toString(),
                                            new Date(System.currentTimeMillis()),
                                            PageRequest.of(offset/limit, limit));
                            break;

            case "best": posts = postsRepository.getBestPosts(
                                            1,
                                            ModerationStatuses.ACCEPTED.toString(),
                                            new Date(System.currentTimeMillis()),
                                            PageRequest.of(offset/limit, limit));
                            break;

            case "early": posts = postsRepository.findByIsActiveAndModerationStatusAndTimeBefore(
                                             1,
                                             ModerationStatuses.ACCEPTED.toString(),
                                             new Date(System.currentTimeMillis()),
                                             PageRequest.of(offset/limit, limit, Sort.by("time")));
                            break;
        }

        if (posts.size() != 0) initPosts(posts);
        return posts;
    }


    public Post getPostByTitle(String title) {
        return postsRepository.findByTitle(title);
    }


    private String getAnnounce(Post post) {
        String text = Jsoup.parse(post.getText()).text();
        return ((text.length() > announceSize) ? text.substring(0, announceSize) : text);
    }


    private List<Post> getActivePostsByQuery(String query, int offset, int limit) {
        List<Post> posts = postsRepository
                .findActivePostsByQuery(
                        1,
                        ModerationStatuses.ACCEPTED.toString(),
                        new Date(System.currentTimeMillis()),
                        "%" + query + "%",
                        PageRequest.of(offset/limit, limit, Sort.by("time"))
                );
        initPosts(posts);
        return posts;
    }


    @SneakyThrows
    private List<Post> getActivePostByDate(String date) {
        //парсим текущую дату из строки
        Calendar thisDay = Calendar.getInstance();
        thisDay.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
        //для sql-запроса получаем 2 даты:
        // - 23:59:59 текущего дня
        thisDay.set(Calendar.HOUR, 23);
        thisDay.set(Calendar.MINUTE, 59);
        thisDay.set(Calendar.SECOND, 59);
        Date dayAfter = thisDay.getTime();
        // - 23:59:59 предыдущего дня
        thisDay.roll(Calendar.DATE, -1);
        Date dayBefore = thisDay.getTime();

        List<Post> posts = postsRepository.findByIsActiveAndModerationStatusAndTimeAfterAndTimeBefore(
                1,
                ModerationStatuses.ACCEPTED.toString(),
                dayBefore,
                dayAfter);

        for (Post post : posts) {
            initPost(post);
        }
        return posts;
    }


    private List<Post> getPostsByUserId(int userId) {
        return postsRepository.findByUserId(userId);
    }


    private int getLikesDislikesCount(Post post, int value) {
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


    private int getCommentsCount(Post post) {
        return post.getPostComments() == null ? 0 : post.getPostComments().size();
    }


    private List<String> getPostTagsList(Post post) {
        List<String> list = new ArrayList<>();
        for (TagToPost tagToPost : post.getTagToPosts()) {
            list.add(tagService.getTagById(tagToPost.getTagId()).getName());
        }
        return list;
    }
//================================= /GETTERS ===========================================================================

//================================= CRUD ===============================================================================
    @Transactional
    public void deletePost(Post post) {
        initPost(post);
        Post postDB = postsRepository.findByTitle(post.getTitle());
        if (postDB != null) postsRepository.delete(postDB);
        for (TagToPost tagToPost: post.getTagToPosts()) {
            Tag tag = tagService.getTagById(tagToPost.getTagId());
            tagToPostService.deleteTagToPost(tagToPost);
            if (tagToPostService.getTagToPostByTagId(tag.getId()).size() == 0) tagService.deleteTag(tag);
        }
    }


    @Transactional
    public boolean savePost(long timestamp, int isActive, String title, String text, int userId, List<String> tagsNames) {
        try {
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
            } else post.setModerationStatus(ModerationStatuses.ACCEPTED.toString());

            post.setViewCount(0);
            //отправляем в репозиторий
            postsRepository.save(post);

            //сохраним тэги и привяжем их к посту
            for (String tagName : tagsNames) {
                tagService.saveTag(tagName, post.getId());
            }
        }
        catch (Exception e) { return false; }

        return true;
    }


    private int countActivePosts() {
        return postsRepository.findByIsActiveAndModerationStatusAndTimeBefore(
                1,
                ModerationStatuses.ACCEPTED.toString(),
                new Date(System.currentTimeMillis()),
                null).size();
    }


    private int countActivePostsByQuery(String query) {
        return postsRepository
                .findActivePostsByQuery(
                        1,
                        ModerationStatuses.ACCEPTED.toString(),
                        new Date(System.currentTimeMillis()),
                        "%" + query + "%",
                        null).size();
    }


    private boolean isPostActive(Post post) {
        //проверяем выполнение сразу 3х условий
        //1 - стоит галочка "пост активен"
        //2 - проверяем статус -> д.б. ACCEPTED
        //3 - проверяем дату публикации -> д.б. не в будующем
        return post.getIsActive() == 1 &&
                post.getModerationStatus().equals(ModerationStatuses.ACCEPTED.toString())
                && post.getTimestamp() <= (System.currentTimeMillis() / 1000);
    }


    @Transactional
    private boolean editPost(int id, long timestamp, int isActive, String title, String text, int userId,
                             List<String> tagsNames) {
        //находим в базе
        Post post = getPostById(id);
        if (post == null) return false;
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
        postsRepository.save(post);

        //сохраним тэги и привяжем их к посту
        for (String tagName : tagsNames) {
            tagService.saveTag(tagName, post.getId());
        }
        return true;
    }


    public boolean setModerationStatus(int postId, String status, int moderatorId) {
        Post post = getPostById(postId);
        if (post == null) return false;
        post.setModerationStatus(status);
        post.setModeratorId(moderatorId);
        postsRepository.save(post);
        return true;
    }


    private void initPost(Post post) {
        if (post.getModeratorId() != null) {
            post.setModeratorPost(userService.getUserById(post.getModeratorId()));
        }

        post.setUserPost(userService.getUserById(post.getUserId()));

        post.setPostVotes(postVoteService.getPostVotesByPostId(post.getId()));

        post.setPostComments(postCommentService.getPostCommentsByPostId(post.getId()));

        post.setTagToPosts(tagToPostService.getTagToPostsByPostId(post.getId()));
    }


    private void initPosts(List<Post> posts) {
        for (Post post : posts) {
            initPost(post);
        }
    }
//================================= /CRUD ==============================================================================

    private List<Post> sortPostList(List<Post> posts, int offset, int limit) {
        return posts.stream()
                .sorted(Comparator.comparing(Post::getTime).reversed())
                .skip(offset)
                .limit(limit).collect(Collectors.toList());
    }


    private List<PostResponse> responsePosts(List<Post> posts) {
        List<PostResponse> list = new ArrayList<>();
        //приводим к нужному виду
        for (Post post : posts) {
            list.add(new PostResponse(
                    post.getId(),
                    post.getTimestamp(),
                    new UserIdNameResponse(post.getUserPost().getId(), post.getUserPost().getName()),
                    post.getTitle(),
                    getAnnounce(post),
                    getLikesDislikesCount(post,1),
                    getLikesDislikesCount(post,-1),
                    getCommentsCount(post),
                    post.getViewCount()
            ));
        }
        return list;
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

//================================= ResponseEntity =====================================================================

    public ResponseEntity<?> postApiPost(RequestPostPutApiPost requestBody) {
        return postPutApiPost(requestBody, null);
    }


    public ResponseEntity<?> putApiPost(RequestPostPutApiPost requestBody, int postId) {
        return postPutApiPost(requestBody, postId);
    }


    @Transactional
    private ResponseEntity<?> postPutApiPost(RequestPostPutApiPost requestBody, Integer postId) {
        // @Secured(USER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        User user = userService.findUserByLogin(authentication.getName());
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

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
        if (text.length() > 16777215)
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResultFalseErrorsResponse(
                            ErrorsResponse.builder().text("Текст публикации слишком длинный").build()
                    ));

        //ошибок нет - добавляем пост
        int userId = user.getId();
        int isActive = requestBody.getActive();
        List<String> tagsNames = requestBody.getTags();

        //POST /api/post  -> postId = null -> создаем новый
        //PUT /api/post/{ID}  -> postId != null -> изменяем
        boolean result;
        if (postId == null) {
            result = savePost(timestamp, isActive, title, text, userId, tagsNames);
        }
        else result = editPost(postId, timestamp, isActive, title, text, userId, tagsNames);

        if (!result) return ResponseEntity.status(HttpStatus.OK)
                .body(new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().text("Не удалось сохранить пост").build()
                ));

        //возвращаем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ResultResponse(true));
    }


    public ResponseEntity<?> getApiPost(int offset, int limit, String mode) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiPostResponse(
                        countActivePosts(),
                        responsePosts(getActivePosts(offset, limit, mode))));
    }


    public ResponseEntity<?> getApiPostSearch(int offset, int limit, String query) {
        if (query == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        //создадим список для найденных постов findPosts
        List<Post> findPosts;

        //если запрос пустой, метод должен выводить все посты
        if (query.length() == 0) {
            findPosts = getActivePosts(offset, limit, "");
        }
        else { //иначе ищем query в текстовых полях
            findPosts = getActivePostsByQuery(query, offset, limit);
        }

        //собираем ответ
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiPostResponse(countActivePostsByQuery(query), responsePosts(findPosts)));
    }


    public ResponseEntity<?> getApiPostId(int id) {
        //ищем нужный пост по id
        Post post = getInitPostById(id);
        //если пост не найден - возвращаем 404
        if (post == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        //При успешном запросе увеличиваем количество просмотров поста на 1, кроме случаев:
        // - Если модератор авторизован, то не считаем его просмотры вообще
        // - Если автор авторизован, то не считаем просмотры своих же публикаций
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        User user = userService.findUserByLogin(authentication.getName());

        //смотрит не аноним
        if (user != null) {
            //автор смотрит без условий
            if (post.getUserId() == user.getId())
                return ResponseEntity.status(HttpStatus.OK).body(postByIdToJSON(post));

            //если модератор - проверяем дату и isActive
            if (user.getIsModerator() == 1 &&
                    post.getIsActive() == 1 &&
                    post.getTimestamp() <= (System.currentTimeMillis() / 1000))
                return ResponseEntity.status(HttpStatus.OK).body(postByIdToJSON(post));
        }

        //если не автор и не модератор - проверяем активность и плюсуем просмотры
        if (isPostActive(post)) {
            post.setViewCount(post.getViewCount() + 1);
            postsRepository.save(post);
            return ResponseEntity.status(HttpStatus.OK).body(postByIdToJSON(post));
        }

        //посторонний пытается посмотреть не активный пост
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    @SneakyThrows
    public ResponseEntity<?> getApiPostByDate(int offset, int limit, String date) {
        //получаем список постов за дату
        List<Post> posts = getActivePostByDate(date);
        //запоминаем размер
        int postsSize = posts.size();
        //сортируем и обрезаем
        posts = sortPostList(posts, offset, limit);
        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(postsSize, responsePosts(posts)));
    }


    public ResponseEntity<?> getApiPostByTag(int offset, int limit, String tagName) {
        List<Post> posts = new ArrayList<>();

        //получаем список тэг-пост для тега по имени тэга
        for (TagToPost tagToPost : tagService.getTagToPost(tagName)) {
            //находим пост по id
            Post post = getInitPostById(tagToPost.getPostId());
            if (post != null) {
                //проверяем на активность
                if (isPostActive(post)) {
                    //и запоминаем
                    posts.add(post);
                }
            }
        }

        //запоминаем размер
        int postsSize = posts.size();
        //сортируем и обрезаем
        posts = sortPostList(posts, offset, limit);
        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(postsSize, responsePosts(posts)));
    }


    public ResponseEntity<?> getApiPostModeration(int offset, int limit, String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        //выдергиваем из контекста пользователя
        User user = userService.findUserByLogin(authentication.getName());
        if (user == null || user.getIsModerator() != 1)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        //переходим на верхний регистр
        status = status.toUpperCase();
        //получим список постов
        List<Post> posts = new ArrayList<>();
        for (Post post : postsRepository.findByModerationStatus(status)) {
            //отбираем активные посты с нужным статусом:
            //NEW - выводим все
            //ACCEPTED-DECLINED -> выводим только для текущего пользователя
            if (post.getIsActive() == 1) {
                initPost(post);
                if (status.equals(ModerationStatuses.NEW.toString())) posts.add(post);
                else if (post.getModeratorId() != null && post.getModeratorId() == user.getId()) posts.add(post);
            }
        }
        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiPostResponse(posts.size(), responsePosts(sortPostList(posts, offset, limit))));
    }


    public ResponseEntity<?> getApiPostMy(int offset, int limit, String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        //получим список постов пользователя
        List<Post> posts = new ArrayList<>();
        for (Post post : getPostsByUserId(userService.findUserByLogin(authentication.getName()).getId())) {
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

        //запоминаем размер -> сортируем и обрезаем
        int postsSize = posts.size();
        posts = sortPostList(posts, offset, limit);
        //собираем ответ
        return ResponseEntity.status(HttpStatus.OK).body(new ApiPostResponse(postsSize, responsePosts(posts)));
    }


    public ResponseEntity<?> postApiModeration(RequestApiModeration requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        //переводим decision -> status
        String status = "";
        String decision = requestBody.getDecision();
        if (decision == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (decision.equals(Decision.ACCEPT.getDecision())) status = ModerationStatuses.ACCEPTED.toString();
        else if (decision.equals(Decision.DECLINE.getDecision())) status = ModerationStatuses.DECLINED.toString();

        //проверяем на ошибки: не найден модератор, не правильный статус, не найден пост
        boolean hasErrors = false;
        User moderator = userService.findUserByLogin(authentication.getName());

        if (moderator == null || moderator.getIsModerator() != 1 || status.equals("")) hasErrors = true;
        else if (!setModerationStatus(requestBody.getPostId(), status, moderator.getId())) hasErrors = true;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        User user = userService.findUserByLogin(authentication.getName());
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        int postsCount = 0;
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        long firstPublication = System.currentTimeMillis() / 1000;

        for (Post post : getPostsByUserId(user.getId())) {
            //учитываем только активные посты
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


        if (postsCount == 0) firstPublication = 0;

        return new ResponseEntity<>(new ApiStatisticsResponse(postsCount, likesCount, dislikesCount,
                viewsCount, firstPublication), HttpStatus.OK);
    }


    public ResponseEntity<?> getApiStatisticsAll() {
        //if STATISTICS_IS_PUBLIC = NO & Auth=false -> HTTP.401
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (globalSettingService.findGlobalSettingByCode(
                        GlobalSettingsCodes.STATISTICS_IS_PUBLIC.toString()).getValue()
                            .equals(GlobalSettingsValues.NO.toString()))
            if (authentication == null || userService.findUserByLogin(authentication.getName()) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        //init parameters
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;
        //timestamp of first publication
        long firstPublication = System.currentTimeMillis() / 1000;
        List<Post> activePosts = getInitActivePosts();

        //перебираем все активные посты
        for (Post post : activePosts) {
            //считаем общ. кол-во лайков
            likesCount += getLikesDislikesCount(post,1);
            //считаем общее кол-во дислайков
            dislikesCount += getLikesDislikesCount(post,-1);
            //считаем общее кол-во просмотров
            viewsCount += post.getViewCount();
            //ищем дату самого первого поста
            if (firstPublication > post.getTimestamp()) firstPublication = post.getTimestamp();
        }
        //считаем кол-во активных постов
        int postsCount = activePosts.size();

        //собираем ответ и возвращаем его
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiStatisticsResponse(postsCount, likesCount, dislikesCount, viewsCount,
                        (postsCount == 0) ? 0 : firstPublication));
    }
}