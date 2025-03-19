/*
 * Copyright 2025 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept

import de.gematik.test.erezept.transformation.cardimage.CardTransformation
import de.gematik.test.erezept.transformation.json.ImageFileTransformation
import java.nio.file.Paths

fun main(args: Array<String>) {
  // TODO handle program arguments
  val source = Paths.get(args[0])
  val destination = Paths.get(System.getProperty("user.dir")).resolve(args[1])
  source.getCertificateElements().let {
    CardTransformation(it).transform(destination)
    ImageFileTransformation(it).transform(destination)
  }
}