package org.orthomcl.service.core.phyletic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamHandler;
import org.gusdb.wdk.model.query.param.StringParamHandler;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;

public class ExpressionParamHandler extends StringParamHandler {

    private static final Logger LOG = Logger.getLogger(ExpressionParamHandler.class);

    private static final String TAXON_SQL = "SELECT three_letter_abbrev "
            + " FROM apidb.orthomcltaxon ORDER BY three_letter_abbrev";
    private static final String GROUP_SQL = "SELECT ortholog_group_id "
            + " FROM apidb.GroupTaxonMatrix";

    private static final String COLUMN_PREFIX = "column";

    private final ExpressionParser _parser = new ExpressionParser();
    private Map<String, Integer> _terms;

    public ExpressionParamHandler() { }

    public ExpressionParamHandler(ExpressionParamHandler handler, Param param) {
       super(handler, param);
    }

    @Override
    public ParamHandler clone(Param paramToClone) {
        return new ExpressionParamHandler(this, paramToClone);
    }

    @Override
    public String toInternalValue(RunnableObj<QueryInstanceSpec> contextParamValues) throws WdkModelException {

        String stableValue = contextParamValues.get().get(_param.getName());

        LOG.debug("transforming phyletic param: " + stableValue);

        _terms = getTerms();

        // parse the expression and get the tree structure
        ExpressionNode root = _parser.parse(stableValue);

        StringBuilder sql = new StringBuilder(GROUP_SQL);
        sql.append(" WHERE " + composeSql(root));

        return sql.toString();
    }

    private Map<String, Integer> getTerms() throws WdkModelException {
        Map<String, Integer> terms = new LinkedHashMap<String, Integer>();
        DataSource dataSource = _wdkModel.getAppDb().getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet =
                    SqlUtils.executeQuery(dataSource, TAXON_SQL,
                            "ortho-exp-param-handler-taxon", 1000);
            int order = 0;
            while (resultSet.next()) {
                String term = resultSet.getString(1);
                terms.put(term.toLowerCase(), order++);
            }
        }
        catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(resultSet, null);
        }
        return terms;
    }

    private String composeSql(ExpressionNode node) throws WdkModelException {
        StringBuilder sql = new StringBuilder("(");
        if (node instanceof BooleanNode) {
            BooleanNode booleanNode = (BooleanNode) node;
            sql.append(composeSql(booleanNode.getLeft()));
            sql.append(" " + booleanNode.getOperator() + " ");
            sql.append(composeSql(booleanNode.getRight()));
        }
        else {
            LeafNode leaf = (LeafNode) node;
            List<String> terms = leaf.getTerms();
            if (terms.size() > 0) sql.append("(");
            StringBuilder sqlTerms = new StringBuilder();
            for (String term : terms) {
                if (sqlTerms.length() > 1) sqlTerms.append(" + ");
                String column = getColumn(term, leaf.isOnSpecies());
                sqlTerms.append(column);
            }
            sql.append(sqlTerms);
            if (terms.size() > 0) sql.append(")");
            sql.append(" " + leaf.getCondition() + leaf.getCount());
        }
        sql.append(")");
        return sql.toString();
    }

    private String getColumn(String term, boolean onSpecies)
            throws WdkModelException {
        if (!_terms.containsKey(term))
            throw new WdkModelException("Invalid expression. Unknown term: " + term);
        int columnIndex = _terms.get(term) * 2 + 1;
        if (onSpecies) columnIndex++;
        return COLUMN_PREFIX + columnIndex;
    }
}
