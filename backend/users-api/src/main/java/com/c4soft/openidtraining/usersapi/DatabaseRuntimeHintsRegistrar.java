package com.c4soft.openidtraining.usersapi;

import org.hibernate.dialect.H2Dialect;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class DatabaseRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    hints.reflection()
        .registerType(H2Dialect.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
  }
}
