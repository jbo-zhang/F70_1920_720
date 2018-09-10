/*
 *  Copyright (C) 2016 BoBoMEe
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hwatong.f70.observable;

import java.util.Set;

public interface ObservableInterface<T, P, R> {

  /**
   *
   */
  void registerObserver(String name, T observer);

  /**
   *
   */
  void removeObserver(String name);

  /**
   *
   */
  void removeObserver(T observer);

  /**
   *
   */
  void removeObserver(String name, T observer);

  /**
   *
   */
  Set<T> getObserver(String name);

  /**
   *
   */
  void clear();

  /**
   *
   *
   */

  R notify(String name, P... p);
}
