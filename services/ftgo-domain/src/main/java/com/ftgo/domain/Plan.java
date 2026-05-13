package com.ftgo.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;
import javax.persistence.ElementCollection;

@Embeddable
public class Plan {

  @ElementCollection private List<Action> actions = new LinkedList<>();

  public void add(Action action) {
    actions.add(action);
  }

  public void removeDelivery(Order order) {
    actions =
        actions.stream()
            .filter(action -> !action.actionFor(order))
            .collect(Collectors.toList());
  }

  public List<Action> getActions() {
    return actions;
  }

  public List<Action> actionsForDelivery(Order order) {
    return actions.stream().filter(action -> action.actionFor(order)).collect(Collectors.toList());
  }
}
