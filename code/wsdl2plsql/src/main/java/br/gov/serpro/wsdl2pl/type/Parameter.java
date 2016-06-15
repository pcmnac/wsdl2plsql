package br.gov.serpro.wsdl2pl.type;

import javax.xml.namespace.QName;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.U;

public class Parameter extends DerivedSymbol
{
    public static enum Direction
    {
        IN("IN"), OUT("OUT"), IN_OUT("IN OUT");

        private String textForm;

        private Direction(String textForm)
        {
            this.textForm = textForm;
        }

        @Override
        public String toString()
        {
            return textForm;
        }
    }

    private ITypeDef type;
    private Function owner;
    private Direction direction;
    private boolean header;

    public Parameter(Context context, Function owner, ElementInfo element)
    {
        this(context, owner, element, Direction.IN, false);
    }

    public Parameter(Context context, Function owner, ElementInfo element, boolean header)
    {
        this(context, owner, element, Direction.IN, header);
    }

    public Parameter(Context context, Function owner, ElementInfo element, Direction direction)
    {
        this(context, owner, element, direction, false);
    }

    public Parameter(Context context, Function owner, ElementInfo element, Direction direction, boolean header)
    {
        super(context, element);
        this.direction = direction;
        this.owner = owner;
        this.header = header;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public Function getOwner()
    {
        return owner;
    }

    public ITypeDef getType()
    {
        return type;
    }

    public void setType(ITypeDef type)
    {
        this.type = type;
    }

    public boolean isHeader()
    {
        return header;
    }

    public String decl()
    {
        String direction = "";
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        switch (getDirection())
        {
            case IN:
                direction = ke.in();
                break;
            case OUT:
                direction = ke.out();
                break;
            case IN_OUT:
                direction = ke.inOut();
                break;
        }

        return String.format("%s %s %s", name(), direction, getType().emit().replaceAll("\\([\\W\\w]+\\)", ""));
    }

    public String name()
    {
        String prefix = getContext().getPrefix(getElement().getNamespace());
        return U.toPlIdentifier(
                getContext().getSymbolNameEmitter().param(prefix, getElement().getName(), getDirection(), isHeader(), getType(),
                        getOwner().getId()), null, getOwner().getId());
    }

    public String comments()
    {
        return new QName(getElement().getNamespace(), getElement().getName(), getContext().getPrefix(getElement().getNamespace())).toString();
    }

}
