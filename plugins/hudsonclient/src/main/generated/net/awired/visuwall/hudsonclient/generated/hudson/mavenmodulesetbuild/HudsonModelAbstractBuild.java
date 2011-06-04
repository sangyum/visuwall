/**
 *     Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com> - Arnaud LEMAIRE <alemaire at norad dot fr>
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.21 at 12:21:30 PM CET 
//


package net.awired.visuwall.hudsonclient.generated.hudson.mavenmodulesetbuild;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hudson.model.AbstractBuild complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hudson.model.AbstractBuild">
 *   &lt;complexContent>
 *     &lt;extension base="{}hudson.model.Run">
 *       &lt;sequence>
 *         &lt;element name="builtOn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="changeSet" type="{}hudson.scm.ChangeLogSet" minOccurs="0"/>
 *         &lt;element name="culprit" type="{}hudson.model.User" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hudson.model.AbstractBuild", propOrder = {
    "builtOn",
    "changeSet",
    "culprit"
})
@XmlSeeAlso({
    HudsonMavenAbstractMavenBuild.class
})
public class HudsonModelAbstractBuild
    extends HudsonModelRun
{

    protected String builtOn;
    protected HudsonScmChangeLogSet changeSet;
    protected List<HudsonModelUser> culprit;

    /**
     * Gets the value of the builtOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuiltOn() {
        return builtOn;
    }

    /**
     * Sets the value of the builtOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuiltOn(String value) {
        this.builtOn = value;
    }

    /**
     * Gets the value of the changeSet property.
     * 
     * @return
     *     possible object is
     *     {@link HudsonScmChangeLogSet }
     *     
     */
    public HudsonScmChangeLogSet getChangeSet() {
        return changeSet;
    }

    /**
     * Sets the value of the changeSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link HudsonScmChangeLogSet }
     *     
     */
    public void setChangeSet(HudsonScmChangeLogSet value) {
        this.changeSet = value;
    }

    /**
     * Gets the value of the culprit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the culprit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCulprit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HudsonModelUser }
     * 
     * 
     */
    public List<HudsonModelUser> getCulprit() {
        if (culprit == null) {
            culprit = new ArrayList<HudsonModelUser>();
        }
        return this.culprit;
    }

}
