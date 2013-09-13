package br.gov.serpro.wsdl2pl.writer;

import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.parser.Context;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Type;
import br.gov.serpro.wsdl2pl.util.SB;

public class SpecWriter extends BaseWriter
{
    private Context context;

    public SpecWriter(Context context)
    {
        this.context = context;
    }

    public String writeSpec()
    {
        SB spec = new SB();
        IKeywordEmitter ke = context.getKeywordEmitter();

        // TODO: tratar
        String packageName = context.getPackageName();

        // CREATE OR REPLACE PACKAGE packageName AS
        spec.l("%s %s %s %s\n", ke.createOrReplace(), ke.packageKey(), packageName, ke.as());

        // TODO: tratar
        spec.l(1, "-- TYPES\n");

        for (Type plType : context.getComplexTypeMap().values())
        {
            spec.l(1, "-- " + plType.comments());
            spec.l(1, plType.forwardDecl() + "\n");
        }

        for (Type plType : context.getComplexTypeMap().values())
        {
            spec.l(1, "-- " + plType.comments());
            spec.l(plType.decl(1));
        }

        // TODO: tratar
        spec.l(1, "-- FUNCTIONS\n");

        if (!context.getPlFunctions().isEmpty())
        {
            for (Function function : context.getPlFunctions())
            {
                spec.l(BODY_INDENT, "--" + function.comments());
                spec.l(function.decl(BODY_INDENT) + ";\n");
            }
        }

        // END packageName;
        spec.l(String.format("%s %s;", ke.end(), packageName));

        return spec.toString();
    }

}
