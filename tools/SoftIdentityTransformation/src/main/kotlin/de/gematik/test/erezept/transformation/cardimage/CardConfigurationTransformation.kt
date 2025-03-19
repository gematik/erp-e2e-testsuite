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

package de.gematik.test.erezept.transformation.cardimage

import de.gematik.test.erezept.transformation.CardType
import de.gematik.test.erezept.transformation.Transformation
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

class CardConfigurationTransformation(
        private val cardImageFilename: String,
        private val cardType: CardType,
        private val iccsn: String,
) : Transformation {
  private val log = LoggerFactory.getLogger(javaClass)
  private val doc: XmlDocument = XmlDocument("card_configuration_template.xml")

  override fun transform(outputPath: Path) {
    log.info("Create a card configuration file ${cardType.name.lowercase()}_${iccsn}.xml")

    val folder = Files.createDirectories(outputPath.resolve("CardSimulationConfigurations"))
    doc.attribute("cardImageFile").let {
      it.textContent = "${it.textContent}${cardImageFilename}"
    }
    doc.attribute("channelContextFile").textContent = "/opt/cats-configuration/card-simulation/CardSimulationConfigurations/ChannelContext_G2_4Channnel.xml"
    doc.save(folder.resolve("configuration_${cardType.name.lowercase()}_${iccsn}.xml"))
  }

}