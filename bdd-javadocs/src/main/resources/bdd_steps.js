
/*
 * Copyright (c) 2022 gematik GmbH
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

document.addEventListener("DOMContentLoaded", event => {
    const gherkin_keyword_pattern = /^(@Angenommen|@Wenn|@Und|@Dann).*/i
    const method_signature_splitter = /^(@Angenommen|@Wenn|@Und|@Dann)\("(.+)"\).*(public&nbsp;void&nbsp;)(.+)(\(.*\))/i
    const signatures = Array.from(document.getElementsByClassName("methodSignature"))
        .filter(elem => elem.innerHTML.match(gherkin_keyword_pattern))

    signatures.forEach(element => {
        const content = element.innerHTML.replace(/\n|\r/g, "") // remove possible linebreaks
        const tokens = content.match(method_signature_splitter)
        const cucumber_keyword = tokens[1]
        const cucumber_regex = fixUmlaute(tokens[2])
        const java_keywords = tokens[3]
        const java_method_name = tokens[4]
        const java_parameters = tokens[5]

        const annotation = document.createElement('div')
        annotation.className = 'cucumber-annotation'
        annotation.innerHTML = `<span class='cucumber-keyword'>${cucumber_keyword}</span>("<span class='cucumber-regex'>${cucumber_regex}</span>")`

        const method_signature = document.createElement('div')
        method_signature.className = 'java-method-wrapper'
        method_signature.innerHTML = `<span class='java-keyword'>${java_keywords}</span><span class='java-method-name'>${java_method_name}</span>${java_parameters}`

        const wrapper = document.createElement('div')
        wrapper.className = 'cucumber-wrapper'
        wrapper.appendChild(annotation)
        wrapper.appendChild(method_signature)

        const parent = element.parentNode
        parent.insertBefore(wrapper, element)
        parent.removeChild(element)
    })
})

/**
 * JavaDoc seems to have a bug with Umlaute within method-annotations. Having Umlaute within the
 * annotations results in wrongly escaped unicode-characters. This method replaces these with the corresponding characters
 * @param text which need to be fixed
 * @returns {*} the fixed text
 */
const fixUmlaute = (text) => {
    return text
        .replace("\\u00f6", "ö")
        .replace("\\u00d6", "Ö")
        .replace("\\u00e4", "ä")
        .replace("\\u00c4", "Ä")
        .replace("\\u00fc", "ü")
        .replace("\\u00dc", "Ü")
        .replace("\\u00df", "ß")
}
