package com.google.gwt.user.cellview.client;

import org.testng.annotations.Test;

public class CellTreeTableModelManagerTest {
    @Test
    public void testLogicalInsert() throws Exception {

        DataNode<Long> sum = new DataNode<Long>();
        CellTreeTableModelManager.logicalInsert(sum, 100L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 200L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 0L,-1);

        CellTreeTableModelManager.logicalInsert(sum, 101L,-1);
        DataNode<Long> bar = CellTreeTableModelManager.logicalInsert(sum, 200L,-1);
        DataNode<Long> barbar = CellTreeTableModelManager.logicalInsert(bar, 200200L,-1);
        CellTreeTableModelManager.logicalInsert(bar, 200201L,-1);
        CellTreeTableModelManager.logicalInsert(barbar, 10200200L,-1);
        CellTreeTableModelManager.logicalInsert(bar, 1000200201L,-1);

        assert sum.getFirstChild() != null;
        assert sum.getFirstChild().getValue() == 100L;
        assert sum.getFirstChild().getNextSibling() != null;
        assert sum.getFirstChild().getNextSibling().getValue() == 200L;
        assert sum.getFirstChild().getNextSibling().getNextSibling() != null;
        assert sum.getFirstChild().getNextSibling().getNextSibling().getValue() == 0L;
        assert sum.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getValue() == 101L;
        assert sum.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getValue() == 200L;

        assert sum.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getValue() == 200200L;
        assert sum.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling().getValue() == 200201L;
        assert sum.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getFirstChild().getValue() == 10200200L;
        assert sum.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling().getValue() == 200201L;
    }

    @Test
    public void testWalk1() {
        DataNode<Long> sum = new DataNode<Long>();
        CellTreeTableModelManager.logicalInsert(sum, 100L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 200L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 0L,-1);
        Visitor<Long> visitor = new Visitor<Long>() {
            public boolean visit(int row, DataNode<Long> visitNode, int indentLevel) {
                return ! (row == 1) ;
            }
        };
        TreeContext<Long> result = CellTreeTableModelManager.walk(sum, visitor, true, 0, 0);
        assert result.getRowIndex() == 1;
        long resultv = result.getData().getValue();
        assert resultv == 100L : "Expected 100, but got " + resultv;
    }

    @Test
    public void testWalk2() {
        DataNode<Long> sum = new DataNode<Long>();
        DataNode<Long> f = CellTreeTableModelManager.logicalInsert(sum, 100L,-1);
        CellTreeTableModelManager.logicalInsert(f, 200L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 20L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 0L,-1);
        Visitor<Long> visitor = new Visitor<Long>() {
            public boolean visit(int row, DataNode<Long> visitNode, int indentLevel) {
                return ! (row == 2) ;
            }
        };
        TreeContext<Long> result = CellTreeTableModelManager.walk(sum, visitor, true, 0, 0);
        assert result.getRowIndex() == 2: "Expected row 2, but got " + result.getRowIndex();
        long resultv = result.getData().getValue() ;
        assert resultv == 200L: "Expected 200, but got " + resultv ;
    }



    @Test
    public void testWalk3() {
        DataNode<Long> sum = new DataNode<Long>();
        DataNode<Long> f = CellTreeTableModelManager.logicalInsert(sum, 100L,-1);
        CellTreeTableModelManager.logicalInsert(f, 200L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 20L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 0L,-1);
        Visitor<Long> v = new Visitor<Long>() {
            public boolean visit(int row, DataNode<Long> visitNode, int indentLevel) {
                if (row == 1) {
                    assert visitNode.getValue() == 100L;
                    assert indentLevel == 1;
                }
                if (row == 2) {
                    assert visitNode.getValue() == 200L;
                    assert indentLevel == 2;
                }
                if (row == 3) {
                    assert visitNode.getValue() == 20L;
                    assert indentLevel == 1;
                }
                if (row == 4) {
                    assert visitNode.getValue() == 0L;
                    assert indentLevel == 1;
                }
                return true;
            }
        };
        CellTreeTableModelManager.walk(sum, v, true, 0, 0);
    }


    @Test
    public void testDepthFirst4() {
        DataNode<Long> sum = new DataNode<Long>();
        DataNode<Long> f = CellTreeTableModelManager.logicalInsert(sum, 39L,-1);

        CellTreeTableModelManager.logicalInsert(sum, 11L,-1);
        CellTreeTableModelManager.logicalInsert(sum, 12L,-1);
        
        DataNode<Long> foo = CellTreeTableModelManager.logicalInsert(f, 200L,-1);
        CellTreeTableModelManager.logicalInsert(f, 303L,-1);
        CellTreeTableModelManager.logicalInsert(foo, 3030L,-1);
        CellTreeTableModelManager.logicalInsert(foo, 4040L,-1);
        CellTreeTableModelManager.logicalInsert(f, 500L,-1);

        CellTreeTableModelManager.logicalInsert(sum, 22L,-1);

        /*
           sum --39---200---3030
                 |    |     |
                 11   303   4040
                 |    |
                 12   500
                 |
                 22

           traversal:
           39
           200
           3030
           4040
           303
           500
           11
           12
           22
         */

        Visitor<Long> v = new Visitor<Long>() {
            public boolean visit(int row, DataNode<Long> visitNode, int indentLevel) {
                if (row == 1) {
                    assert visitNode.getValue() == 39L;
                    assert indentLevel == 1;
                }
                if (row == 2) {
                    assert visitNode.getValue() == 200L;
                    assert indentLevel == 2;
                }
                if (row == 3) {
                    assert visitNode.getValue() == 3030L;
                    assert indentLevel == 3;
                }
                if (row == 4) {
                    assert visitNode.getValue() == 4040L;
                    assert indentLevel == 3;
                }

                if (row == 5) {
                    assert visitNode.getValue() == 303L;
                    assert indentLevel == 2;
                }

                if (row == 6) {
                    assert visitNode.getValue() == 500L;
                    assert indentLevel == 2;
                }

                if (row == 7) {
                    assert visitNode.getValue() == 11L;
                    assert indentLevel == 1;
                }


                if (row == 8) {
                    assert visitNode.getValue() == 12L;
                    assert indentLevel == 1;
                }

                if (row == 9) {
                    assert visitNode.getValue() == 22L;
                    assert indentLevel == 1;
                }
                return true;
            }
        };
        CellTreeTableModelManager.walk(sum, v, true, 0, 0);
    }
}
