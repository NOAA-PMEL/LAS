/*
*	Copyright Insight4 Pty Ltd 2005-2007 (http://www.insight4.com)
*	See the COPYRIGHT file supplied with the source distribution for information about
*	distribution and copying of this software.
*/

package au.org.tpac.wms.lib;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Pauline Mak <pauline@insight4.com>
 */
public class WMSContactInfo extends WMSParser
{
    /**
     * Name of the primary contact person
     */
    protected String primaryPerson;

    /**
     * Organisation that the primary contact is aligned with.
     */
    protected String primaryPersonOrg;

    /**
     * Position of the primary contact
     */
    protected String contactPos;

    /**
     * Type of the addresss
     */
    protected String addressType;

    /**
     * First line of the address (street number/unit number, street name)
     */
    protected String address;

    /**
     * Name of the city as part of the contact address
     */
    protected String city;

    /**
     * State or provience that of the contact address
     */
    protected String stateOrProv;

    /**
     * post/zip code of the contact address
     */
    protected String postCode;

    /**
     * Country of the contact address
     */
    protected String country;

    /**
     * Voice number that can be called
     */
    protected String voiceTelephone;

    /**
     * email address of the primary contact
     */
    protected String email;


    /**
     * Empty constructor
     */
    public WMSContactInfo()
    {
        primaryPerson = null;
        primaryPersonOrg = null;
        contactPos = null;
        addressType = null;
        address = null;
        city = null;
        stateOrProv = null;
        postCode = null;
        country = null;
        voiceTelephone = null;
        email = null;
    }

    /**
     * A constructor that takes in an XML node and stores all contact information
     * @param node node to parse
     */
    public WMSContactInfo(Node node)
    {
        primaryPerson = null;
        primaryPersonOrg = null;
        contactPos = null;
        addressType = null;
        address = null;
        city = null;
        stateOrProv = null;
        postCode = null;
        country = null;
        voiceTelephone = null;
        email = null;

        parse(node);
    }

    /**
     * Sets the email address of the primary contact
     * @param _email email address
     */
    public void setEmail(String _email)
    {
        email = _email;
    }

    public String getEmail()
    {
        return email;
    }

    public void setVoiceTelephone(String _voiceTelephone)
    {
        voiceTelephone = _voiceTelephone;
    }


    public String getVoiceTelephone()
    {
        return voiceTelephone;
    }

    public void setCountry(String _country)
    {
        country = _country;
    }

    public String getCountry()
    {
        return country;
    }

    public String getPostCode()
    {
        return postCode;
    }

    public void setPostCode(String _postCode)
    {
        postCode = _postCode;
    }

    public String getStateOrProv()
    {
        return stateOrProv;
    }

    public void setStateOfProv(String _stateOrProv)
    {
        stateOrProv = _stateOrProv;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String _city)
    {
        city = _city;
    }

    public void setAddress(String _address)
    {
        address = _address;
    }


    public String getAddress()
    {
        return address;
    }


    public void setAddressType(String _addressType)
    {
        addressType = _addressType;
    }


    public String getAddressType()
    {
        return addressType;
    }


    public void setContactPos(String _contactPos)
    {
        contactPos = _contactPos;
    }

    public String getContactPos()
    {
        return contactPos;
    }

    public void setPrimaryPersonOrg(String _primaryPersonOrg)
    {
        primaryPersonOrg = _primaryPersonOrg;
    }

    public String getPrimaryPersonOrg()
    {
        return primaryPersonOrg;
    }

    public void setPrimaryPerson(String _primaryPerson)
    {
        primaryPerson = _primaryPerson;
    }

    public String getPrimaryPerson()
    {
        return primaryPerson;
    }


    public void setXMLElementSelf(Document doc)
    {
        this.xmlElementName = "ContactInformation";

        Element primePersonElement = doc.createElement("ContactPersonPrimary");

        if(primaryPerson != null)
        {
            Element person = doc.createElement("ContactPerson");
            Text txtNode = doc.createTextNode(this.primaryPerson);
            person.appendChild(txtNode);
            primePersonElement.appendChild(person);
        }

        if(this.primaryPersonOrg != null)
        {
            Element org = doc.createElement("ContactOrganization");
            Text txtNode = doc.createTextNode(this.primaryPersonOrg);
            org.appendChild(txtNode);
            primePersonElement.appendChild(org);
        }

        innerElements.add(primePersonElement);

        if(contactPos != null)
        {
            Element pos = doc.createElement("ContactPosition");
            Text txtNode = doc.createTextNode(this.contactPos);
            pos.appendChild(txtNode);
            this.innerElements.add(pos);
        }

        Element contactElement = doc.createElement("ContactAddress");

        if(addressType != null)
        {
            Element typeElement = doc.createElement("AddressType");
            Text txtNode = doc.createTextNode(addressType);
            typeElement.appendChild(txtNode);
            contactElement.appendChild(typeElement);
        }

        if(address != null)
        {
            Element typeElement = doc.createElement("Address");
            Text txtNode = doc.createTextNode(address);
            typeElement.appendChild(txtNode);
            contactElement.appendChild(typeElement);
        }

        if(city != null)
        {
            Element typeElement = doc.createElement("City");
            Text txtNode = doc.createTextNode(city);
            typeElement.appendChild(txtNode);
            contactElement.appendChild(typeElement);
        }

        if(stateOrProv != null)
        {
            Element typeElement = doc.createElement("StateOrProvince");
            Text txtNode = doc.createTextNode(stateOrProv);
            typeElement.appendChild(txtNode);
            contactElement.appendChild(typeElement);
        }

        if(postCode != null)
        {
            Element typeElement = doc.createElement("PostCode");
            Text txtNode = doc.createTextNode(postCode);
            typeElement.appendChild(txtNode);
            contactElement.appendChild(typeElement);
        }

        if(country != null)
        {
            Element typeElement = doc.createElement("Country");
            Text txtNode = doc.createTextNode(country);
            typeElement.appendChild(txtNode);
            contactElement.appendChild(typeElement);
        }

        innerElements.add(contactElement);

        if(this.voiceTelephone != null)
        {
            Element typeElement = doc.createElement("ContactVoiceTelephone");
            Text txtNode = doc.createTextNode(voiceTelephone);
            typeElement.appendChild(txtNode);
            innerElements.add(typeElement);
        }


        if(email != null)
        {
            Element typeElement = doc.createElement("ContactElectronicMailAddress");
            Text txtNode = doc.createTextNode(email);
            typeElement.appendChild(txtNode);
            innerElements.add(typeElement);
        }

    }

    protected void saveAttributeData(String attName, String attValue)
    {

    }

    protected void saveNodeData(String nodeName, String nodeValue)
    {
        if(nodeName.equalsIgnoreCase("ContactPerson"))
        {
            this.primaryPerson = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("ContactOrganization"))
        {
            this.primaryPersonOrg = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("ContactPosition"))
        {
            this.contactPos = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("AddressType"))
        {
            this.addressType = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("Address"))
        {
            address = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("StateOrProvince"))
        {
            this.stateOrProv = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("Country"))
        {
            this.country = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("PostCode"))
        {
            this.postCode = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("ContactVoiceTelephone"))
        {
            this.voiceTelephone = nodeValue;
        }
        else if(nodeName.equalsIgnoreCase("ContactElectronicMailAddress"))
        {
            this.email = nodeValue;
        }
    }

    protected boolean processChildren(String childName, Node childNode)
    {
        return false;
    }

}
