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

package de.gematik.test.erezept.transformation.cardimage

import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory


private val log = LoggerFactory.getLogger("XmlDocument")

private val xPathFactory: XPathFactory = XPathFactory.newInstance()
private val xpath: XPath = xPathFactory.newXPath()

private fun Node.search(pattern: String): Node? {
  val expr: XPathExpression = xpath.compile(pattern)
  val nodeList: NodeList? = expr.evaluate(this, XPathConstants.NODESET) as? NodeList
  return nodeList?.item(0)
}

fun Node.element(identifier: String): Node? {
  return this.search("//child[@id='${identifier}']")
          ?: run {
            log.warn("$identifier was not found in template")
            return null
          }
}

fun Node.attribute(identifier: String): Node {
  val prefix = if (this is Document) {
    "//"
  } else {
    "attributes/"
  }
  return this.search("${prefix}attribute[@id='${identifier}']")
          ?: run { throw XmlDocumentException("$identifier was not found") }
}

open class XmlDocument(private val filename: String) {
  private val log = LoggerFactory.getLogger(javaClass)

  private var doc: Document

  init {
    this.doc = loadDocument()
  }

  fun element(identifier: String) = doc.element(identifier)
  fun attribute(identifier: String) = doc.attribute(identifier)


  private fun loadDocument(): Document {
    log.debug("try to open the file $filename")
    val inputStream = this.javaClass.classLoader.getResourceAsStream(filename)
    val xmlDoc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(inputStream)

    xmlDoc.documentElement.normalize()
    return xmlDoc
  }

  fun save(outputPath: Path) {
    val transformerFactory = TransformerFactory.newInstance()
    val transformer: Transformer = transformerFactory.newTransformer()
    val source = DOMSource(doc)
    val streamResult = StreamResult(File("${outputPath.toAbsolutePath()}"))
    transformer.transform(source, streamResult)
  }
}

