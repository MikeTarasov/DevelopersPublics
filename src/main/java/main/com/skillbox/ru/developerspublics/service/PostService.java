package main.com.skillbox.ru.developerspublics.service;

import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsCodes;
import main.com.skillbox.ru.developerspublics.model.enums.GlobalSettingsValues;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.entity.*;
import main.com.skillbox.ru.developerspublics.repository.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    private final int announceSize = 1000;

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
        for (Post postDB : postsRepository.findAll()) {
            if (postDB.getTitle().equals(title)) {
                return postDB;
            }
        }
        return null;
    }

    public List<Post> getActivePosts() {
        List<Post> activePosts = new ArrayList<>();
        for (Post post : getInitPosts()) {
            if (isPostActive(post)) {
                activePosts.add(post);
            }
        }
        return activePosts;
    }

    public List<Post> getInitPosts() {
        List<Post> postList = new ArrayList<>();
        for (Post postDB : postsRepository.findAll()) {
            initPost(postDB);
            postList.add(postDB);
        }
        return postList;
    }

    public List<Post> getPosts() {
        return new ArrayList<>(postsRepository.findAll());
    }

    private void initPost(Post post) {
        if (post.getModeratorId() != null) {
            post.setModeratorPost(userService.getUserById(post.getModeratorId()));
        }

        post.setUserPost(userService.getUserById(post.getUserId()));

        List<PostVote> postVotes = new ArrayList<>();
        for (PostVote postVoteDB : postVoteService.getPostVotes()) {
            if (postVoteDB.getPostId() == post.getId()) {
                postVotes.add(postVoteDB);
            }
        }
        post.setPostVotes(postVotes);

        List<PostComment> postComments = new ArrayList<>();
        for (PostComment postCommentDB : postCommentService.getPostComments()) {
            if (postCommentDB.getPostId() == post.getId()) {
                postComments.add(postCommentDB);
            }
        }
        post.setPostComments(postComments);

        List<TagToPost> tagToPosts = new ArrayList<>();
        for (TagToPost tagToPostDB : tagToPostService.getTagToPosts()) {
            if (tagToPostDB.getPostId() == post.getId()) {
                tagToPosts.add(tagToPostDB);
            }
        }
        post.setTagToPosts(tagToPosts);
    }



    public JSONArray responsePosts(List<Post> posts, int offset, int limit, String mode) {
        List<Post> responsePosts = new ArrayList<>();
        //сортируем -> обрезаем -> переносим в список ответа
        sortedByMode(posts.stream(), mode)
                .skip(offset).limit(limit)
                .forEach(responsePosts::add);

        JSONArray jsonArray = new JSONArray();
        //приводим к нужному виду
        for (Post post : responsePosts) {
            jsonArray.add(postToJSON(post));
        }
        return jsonArray;
    }

    public JSONObject postToJSON(Post post) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", post.getId());
        jsonObject.put("timestamp", post.getTimestamp());

        JSONObject user = new JSONObject();
        user.put("id", post.getUserPost().getId());
        user.put("name", post.getUserPost().getName());
        jsonObject.put("user", user);

        jsonObject.put("title", post.getTitle());
        jsonObject.put("announce", getAnnounce(post));
        jsonObject.put("likeCount", getLikesDislikesCount(post,1));
        jsonObject.put("dislikeCount", getLikesDislikesCount(post,-1));
        jsonObject.put("commentCount", getCommentsCount(post));
        jsonObject.put("viewCount", post.getViewCount());

        return jsonObject;
    }

    public JSONObject postByIdToJSON(Post post) {
        //добавили инфо по посту
        JSONObject jsonObject = postToJSON(post);

        jsonObject.remove("announce");
        jsonObject.put("text", post.getText());
        jsonObject.put("active", post.getIsActive() == 1);

        //добавили инфо по комментариям
        JSONArray comments = new JSONArray();
        for (PostComment postComment : post.getPostComments()) {
            comments.add(postCommentService.postCommentToJSON(postComment));
        }
        jsonObject.put("comments", comments);
        //добавляем инфо по тэгам
        jsonObject.put("tags", new JSONArray().addAll(getPostTagsList(post)));

        return jsonObject;
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
        postsRepository.save(post);

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
}