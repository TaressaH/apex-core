/*
 * Copyright (c) 2013 Malhar Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */
package com.datatorrent.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * Annotation to indicate a jar file dependency that needs to be deployed to cluster when launching the application.<p>
 * <br>
 * Can be used with {@link com.datatorrent.api.Operator} and {@link com.datatorrent.api.StreamCodec} classes that can be
 * configured in the DAG.<br>
 * <br>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ShipContainingJars {
  Class<?>[] classes();
}
