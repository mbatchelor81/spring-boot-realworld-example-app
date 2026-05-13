package com.ftgo.testlib.fixtures;

import com.ftgo.common.Money;
import com.ftgo.domain.MenuItem;
import com.ftgo.domain.Restaurant;
import com.ftgo.testlib.builders.RestaurantBuilder;

/** Pre-built {@link Restaurant} instances for common test scenarios. */
public final class RestaurantFixtures {

  private RestaurantFixtures() {}

  public static Restaurant ajantaRestaurant() {
    return RestaurantBuilder.aRestaurant()
        .withName("Ajanta")
        .withMenuItem("tikka-1", "Chicken Tikka Masala", new Money("12.99"))
        .withMenuItem("vindaloo-1", "Lamb Vindaloo", new Money("14.99"))
        .build();
  }

  public static MenuItem chickenTikkaMasala() {
    return new MenuItem("tikka-1", "Chicken Tikka Masala", new Money("12.99"));
  }

  public static MenuItem lambVindaloo() {
    return new MenuItem("vindaloo-1", "Lamb Vindaloo", new Money("14.99"));
  }
}
