package com.ftgo.common.jpa;

import com.ftgo.common.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Imports {@link CommonConfiguration} and makes JPA utility classes available for component
 * scanning.
 */
@Configuration
@Import(CommonConfiguration.class)
public class JpaConfiguration {}
