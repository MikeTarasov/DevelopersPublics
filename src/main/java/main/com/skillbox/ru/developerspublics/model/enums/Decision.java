package main.com.skillbox.ru.developerspublics.model.enums;

public enum Decision {
  ACCEPT("accept"),
  DECLINE("decline");

  String decision;

  Decision(String decision) {
    this.decision = decision;
  }

  public String getDecision() {
    return decision;
  }
}
