package main.com.skillbox.ru.developerspublics.model.enums;

public enum ModerationStatuses {
    NEW("NEW"),
    ACCEPTED("ACCEPTED"),
    DECLINED("DECLINED");

    String status;
    ModerationStatuses(String status) {
       this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
