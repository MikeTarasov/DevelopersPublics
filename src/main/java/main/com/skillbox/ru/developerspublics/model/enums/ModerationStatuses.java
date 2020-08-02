package main.com.skillbox.ru.developerspublics.model.enums;

public enum ModerationStatuses {
    NEW("new"),
    ACCEPTED("accepted"),
    DECLINED("declined");

    String status;
    ModerationStatuses(String status) {
       this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
