package br.gov.serpro.wsdl2pl.type.def;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.type.validation.InputValidator;
import br.gov.serpro.wsdl2pl.type.validation.InValidator;
import br.gov.serpro.wsdl2pl.util.U;

import com.predic8.schema.SimpleType;
import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.schema.restriction.StringRestriction;
import com.predic8.schema.restriction.facet.EnumerationFacet;
import com.predic8.schema.restriction.facet.Facet;

public class SimpleTypeDef extends XsdTypeDef
{
    private InputValidator inputValidator;

    private int maxSize;

    private XsdTypeDef baseType;

    public SimpleTypeDef(Context context, SimpleType simpleType)
    {
        super(context, simpleType.getRestriction().getBase());

        baseType = new XsdTypeDef(context, simpleType.getRestriction().getBase());

        System.out.println(simpleType.getName());

        BaseRestriction baseRestriction = simpleType.getRestriction();
        System.out.println(baseRestriction.getClass());

        if (baseRestriction instanceof StringRestriction)
        {
            InValidator inValidator = null;

            for (Facet facet : baseRestriction.getFacets())
            {
                if (facet instanceof EnumerationFacet)
                {
                    if (inValidator == null)
                    {
                        inValidator = new InValidator(getContext());
                        inputValidator = inValidator;
                    }
                    maxSize = Math.max(maxSize, facet.getValue().length());
                    inValidator.addValidValue("'" + facet.getValue() + "'");
                }
            }
        }

        for (Facet facet : baseRestriction.getFacets())
        {
            System.out.println(facet + " - " + facet.getValue());
        }

    }

    public InputValidator getInputValidator()
    {
        return inputValidator;
    }

    public XsdTypeDef getBaseType()
    {
        return baseType;
    }

    @Override
    public String emit()
    {
        String size = maxSize != 0 ? "(" + maxSize + ")" : null;

        return U.baseTypeToPlType(getXsdType(), getContext(), size);
    }

}
