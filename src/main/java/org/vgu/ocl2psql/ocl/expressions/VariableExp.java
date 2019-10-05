/*
// * Copyright (c) 2009 by Robert Herschke,
 * Hauptstrasse 30, 65760 Eschborn, rh@ocl.herschke.de.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Robert Herschke. ("Confidential Information").
 */
package org.vgu.ocl2psql.ocl.expressions;

import org.vgu.ocl2psql.ocl.context.OclContext;
import org.vgu.ocl2psql.ocl.deparser.DeparserVisitor;
import org.vgu.ocl2psql.ocl.deparser.OclExpressionDeParser;
import org.vgu.ocl2psql.sql.statement.select.PlainSelect;
import org.vgu.ocl2psql.sql.statement.select.RefSelectExpression;
import org.vgu.ocl2psql.sql.statement.select.ResSelectExpression;
import org.vgu.ocl2psql.sql.statement.select.Select;
import org.vgu.ocl2psql.sql.statement.select.TypeSelectExpression;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;

/**
 * Class VariableExp
 */
public class VariableExp extends OclExpression {
    /**
     * Attribute referredVariable
     */
    private final Variable referredVariable;

    public VariableExp(Variable referredVariable) {
	this.referredVariable = referredVariable;
    }

    @Override
    public Object eval(OclContext context) {
	return context.getVariable(referredVariable.getName());
    }

	public Variable getReferredVariable() {
		return referredVariable;
	}
	
	@Override
	public void accept( DeparserVisitor visitor ) {
	    visitor.visit( this );
	}

    @Override
    public Statement map(StmVisitor visitor) {
        String var_name = this.getReferredVariable().getName();
        IteratorSource iter = null;
        for(IteratorSource iter_item : visitor.getVisitorContext()) {
            if(iter_item.getIterator().getName().equals(var_name)) {
                iter = iter_item;
                break;
            }
        }
        if(iter == null) {
            Select finalSelect = new Select();
            PlainSelect finalPlainSelect = new PlainSelect();
            ResSelectExpression item = new ResSelectExpression();
            item.setExpression(new Column(this.getReferredVariable().getName()));
            finalPlainSelect.setRes(item);
            RefSelectExpression ref = new RefSelectExpression(this.referredVariable.getName());
            ref.setExpression(new Column(this.getReferredVariable().getName()));
            finalPlainSelect.addSelectItems(ref);
            finalSelect.setSelectBody(finalPlainSelect);
            finalPlainSelect.setType(new TypeSelectExpression("Unknown"));
            //Create and add new iterator into visitor context.
            MyIteratorSource newFreeIteratorSource = new MyIteratorSource();
            newFreeIteratorSource.setIterator(new Variable(var_name));
            newFreeIteratorSource.setSource(finalSelect);
            visitor.getVisitorContext().add(newFreeIteratorSource);
            return finalSelect;
        }
        Select finalSelect = (Select) ((MyIteratorSource) iter).getSource();
        PlainSelect finalPlainSelect = (PlainSelect) finalSelect.getSelectBody();
        OclExpressionDeParser oclExpressionDeParser = new OclExpressionDeParser();
        this.accept(oclExpressionDeParser);
        finalPlainSelect.setCorrespondOCLExpression(oclExpressionDeParser.getDeParsedStr());
        return finalSelect;
    }
}
