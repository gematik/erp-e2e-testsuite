/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhirdump;

import com.fasterxml.jackson.annotation.*;
import java.util.*;
import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StepDump {

  private String name;
  private List<FileDump> dumps = new LinkedList<>();

  public void addDump(String operation, String file) {
    val fdf = new FileDump();
    fdf.setOperation(operation);
    fdf.setFile(file);
    this.addDump(fdf);
  }

  public void addDump(FileDump dump) {
    this.dumps.add(dump);
  }
}
