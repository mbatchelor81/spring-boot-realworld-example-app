package com.ftgo.security.authorization;

public final class FtgoPermission {

  public static final String ORDER_CREATE = "ORDER_CREATE";
  public static final String ORDER_READ = "ORDER_READ";
  public static final String ORDER_CANCEL = "ORDER_CANCEL";
  public static final String ORDER_REVISE = "ORDER_REVISE";
  public static final String ORDER_ACCEPT = "ORDER_ACCEPT";
  public static final String ORDER_MANAGE_LIFECYCLE = "ORDER_MANAGE_LIFECYCLE";

  public static final String CONSUMER_CREATE = "CONSUMER_CREATE";
  public static final String CONSUMER_READ = "CONSUMER_READ";

  public static final String RESTAURANT_CREATE = "RESTAURANT_CREATE";
  public static final String RESTAURANT_READ = "RESTAURANT_READ";
  public static final String RESTAURANT_REVISE_MENU = "RESTAURANT_REVISE_MENU";

  public static final String COURIER_CREATE = "COURIER_CREATE";
  public static final String COURIER_READ = "COURIER_READ";
  public static final String COURIER_PLAN_DELIVERY = "COURIER_PLAN_DELIVERY";
  public static final String COURIER_UPDATE_AVAILABILITY = "COURIER_UPDATE_AVAILABILITY";

  private FtgoPermission() {}
}
