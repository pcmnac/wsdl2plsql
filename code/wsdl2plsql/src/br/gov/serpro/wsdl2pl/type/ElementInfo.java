package br.gov.serpro.wsdl2pl.type;

import com.predic8.schema.Element;

/**
 * @author 04343650413
 * 
 */
public class ElementInfo
{
    private String name;

    private String namespace;

    private boolean optional;

    public ElementInfo(String name)
    {
        this(name, null);
    }

    public ElementInfo(String name, String namespace)
    {
        this.name = name;
        this.namespace = namespace;
    }

    public ElementInfo(Element element)
    {
        // this(element.getName(), (element.getSchema() != null && element.getSchema().getElementFormDefault()
        // .equals("qualified")) ? element.getSchema().getTargetNamespace() : null);

        this(element.getName());

        if (element.getSchema() != null)
        {
            if (element.getProperty("form").equals("qualified") || element.isToplevel()) //element.getSchema().getElement(element.getQname()) != null)
            {
                setNamespace(element.getSchema().getTargetNamespace());
            }
            else
            {
                if (element.getSchema().getElementFormDefault().equals("qualified"))
                {
                    setNamespace(element.getSchema().getTargetNamespace());
                }
            }
        }

        // setNamespace((element.getSchema() != null && element.getSchema().getElementFormDefault().equals("qualified"))
        // ? element
        // .getSchema().getTargetNamespace() : null);

        setOptional(Integer.parseInt(element.getMinOccurs()) == 0);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    @Override
    public String toString()
    {
        return (getNamespace() != null ? "{" + getNamespace() + "}" : "") + getName();
    }

}
