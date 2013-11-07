package br.gov.serpro.wsdl2pl.util;

public class SB
{
    private StringBuilder builder;

    public SB()
    {
        this(new StringBuilder());
    }

    public SB(StringBuilder builder)
    {
        this.builder = builder;
    }

    public void a(String string)
    {
        a(0, string);
    }

    public void a(String string, Object... values)
    {
        builder.append(String.format(string, values));
    }

    public void a(int indentLevel, String string)
    {
        indent(indentLevel);
        builder.append(string);
    }

    public void a(int indentLevel, String string, Object... values)
    {
        indent(indentLevel);
        builder.append(String.format(string, values));
    }

    public void l()
    {
        l("");
    }

    public void l(String string)
    {
        l(0, string);
    }

    public void l(String string, Object... values)
    {
        builder.append(String.format(string, values) + "\n");
    }

    public void l(int indentLevel, String string)
    {
        indent(indentLevel);
        builder.append(string + "\n");
    }

    public void l(int indentLevel, String string, Object... values)
    {
        indent(indentLevel);
        builder.append(String.format(string, values) + "\n");
    }

    public void indent(int level)
    {
        for (int i = 0; i < level; i++)
        {
            builder.append("    ");
        }
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

    public static void main(String[] args)
    {
        SB b = new SB(new StringBuilder());

        b.a("teste");
        b.a(1, "teste");
        b.a(1, "<%s-%s>", "teste", "f");
        b.l();
        b.l("teste");
        b.l(1, "teste");
        b.l(1, "<%s-%s>", "teste", "f");
        System.out.println(b.toString());
    }

}
