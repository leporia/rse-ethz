package apron;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for basic Apron functionality, inspired by
 * https://github.com/antoinemine/apron/blob/cf9017f99655e514b1ba336a5c56c548189ccd64/japron/apron/Test.java
 */
public class ApronTest {

    /**
     * Numerical abstract domain to use for analysis: Convex polyhedra
     */
    Manager man = new Polka(true);

    /**
     * Prepare environment with three integer variables x and y
     */
    String[] integer_names = { "x", "y" };
    String[] real_names = {};
    Environment env = new Environment(integer_names, real_names);

    ///////////////////
    // TEST ELEMENTS //
    ///////////////////
    // these three abstract elements are used for all tests in the following

    /**
     * abstract element containing no values
     */
    Abstract1 bottom;

    /**
     * abstract element containing all values
     */
    Abstract1 top;

    /**
     * abstract element containing values {(x,y) | 1 <= x <= 2 and -1 <= y <= 1}
     */
    Abstract1 xy;

    public ApronTest() throws ApronException {
        this.bottom = new Abstract1(man, env, true);
        this.top = new Abstract1(man, env);

        // encode 1 <= x <= 2, -1 <= y <= 1
        Interval[] box = { new Interval(1, 2), new Interval(-1, 1) };
        this.xy = new Abstract1(man, env, integer_names, box);
    }

    ////////////////////
    // TOP AND BOTTOM //
    ////////////////////

    @Test
	public void testTop() throws ApronException {
        Assertions.assertEquals("<universal>", top.toString());
    }

    @Test
    public void testBottom() throws ApronException {
        Assertions.assertEquals("<empty>", bottom.toString());
    }

    @Test
    public void testIsBottom() throws ApronException {
        Assertions.assertFalse(top.isBottom(man), "Top is not bottom");
        Assertions.assertTrue(bottom.isBottom(man), "Bottom is bottom");
    }

    @Test
    public void testIsTop() throws ApronException {
        Assertions.assertTrue(top.isTop(man), "Top is top");
        Assertions.assertFalse(bottom.isTop(man), "Bottom is not top");
    }

    ///////////////////////
    // RELATIONS OF SETS //
    ///////////////////////

    @Test
	public void testIsEqual() throws ApronException {
        Assertions.assertTrue(top.isEqual(man, top));
        Assertions.assertTrue(bottom.isEqual(man, bottom));
    }

    @Test
    public void testIsIncluded() throws ApronException {
        Assertions.assertTrue(top.isIncluded(man, top));
        Assertions.assertTrue(bottom.isIncluded(man, bottom));

        Assertions.assertTrue(bottom.isIncluded(man, top));
        Assertions.assertFalse(top.isIncluded(man, bottom));
    }

    ///////////////
    // INTERVALS //
    ///////////////

    @Test
    public void testIntervals() throws ApronException {
        String expected = "{  -1x +2 >= 0;  -1y +1 >= 0;  1y +1 >= 0;  1x -1 >= 0 }";
        Assertions.assertEquals(expected, this.xy.toString());

        Interval x = this.xy.getBound(man, "x");
        Assertions.assertEquals("[1,2]", x.toString());

        Scalar lowerBound = x.inf();
        Scalar upperBound = x.sup();
        Assertions.assertTrue(lowerBound.isEqual(1));
        Assertions.assertTrue(upperBound.isEqual(2));
    }

    /////////////////////////////////
    // EXPRESSIONS and CONSTRAINTS //
    /////////////////////////////////

    @Test
    public void testExpressions() throws ApronException {
        // encode 2
        Coeff two = new MpqScalar(2);
        Texpr1Node twoNode = new Texpr1CstNode(two);
        // encode x
        Texpr1Node x = new Texpr1VarNode("x");
        // encode 2 * x
        Texpr1Node twoX = new Texpr1BinNode(Texpr1BinNode.OP_MUL, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, twoNode, x);
        
        // encode 2 * x >= 0
        Tcons1 constraint = new Tcons1(env, Tcons1.SUPEQ, twoX);
        Assertions.assertTrue(xy.satisfy(man, constraint));

        // encode -x >= 0
        Texpr1Node minusX = new Texpr1UnNode(Texpr1UnNode.OP_NEG, x);
        Tcons1 constraint2 = new Tcons1(env, Tcons1.SUPEQ, minusX);
        Assertions.assertFalse(xy.satisfy(man, constraint2));

        // encode y >= 0
        Texpr1Node y = new Texpr1VarNode("y");
        Tcons1 constraint3 = new Tcons1(env, Tcons1.SUPEQ, y);
        Assertions.assertFalse(xy.satisfy(man, constraint3)); // constraint may be violated
		
		// meet with constraint
		Abstract1 myBottom = xy.meetCopy(man, constraint2);
		Assertions.assertTrue(myBottom.isEqual(man, this.bottom));

		// TODO: Note that some constraints are handled imprecisely in Apron,
		// but can be expressed differently to improve precision. For example,
		// instead of encoding x - y != 0, it is possible to encode "x - y > 0"
		// and "x - y < 0", and combine these two cases appropriately.
    }

    /////////////////
    // ASSIGNMENTS //
    /////////////////

    @Test
    public void testAssign() throws ApronException {
        // encode 2 * x
        Texpr1Node twoNode = new Texpr1CstNode(new MpqScalar(2));
        Texpr1Node x = new Texpr1VarNode("x");
        Texpr1Node twoX = new Texpr1BinNode(Texpr1BinNode.OP_MUL, Texpr1BinNode.RTYPE_INT, Texpr1BinNode.RDIR_ZERO, twoNode, x);
        Texpr1Intern twoXIntern = new Texpr1Intern(env, twoX);

        Abstract1 afterAssignment = this.xy.assignCopy(man, "y", twoXIntern, null);

        // 1 <= x <= 2, y = 2*x
        Assertions.assertEquals("{  -2x +1y = 0;  -1x +2 >= 0;  1x -1 >= 0 }", afterAssignment.toString());
    }

    //////////////////////////
    // MEET, JOIN and WIDEN //
    //////////////////////////

    @Test
    public void testMeet() throws ApronException {
        Abstract1 stillTop = top.meetCopy(man, top);
        Assertions.assertTrue(top.isEqual(man, stillTop));

        Abstract1 nowBottom = top.meetCopy(man, bottom);
        Assertions.assertTrue(bottom.isEqual(man, nowBottom));
    }

    @Test
    public void testJoin() throws ApronException {
        Abstract1 stillBottom = bottom.joinCopy(man, bottom);
        Assertions.assertTrue(bottom.isEqual(man, stillBottom));

        Abstract1 nowTop = bottom.joinCopy(man, top);
        Assertions.assertTrue(top.isEqual(man, nowTop));
    }

    @Test
    public void testWiden() throws ApronException {
        Abstract1 stillBottom = this.widenFixed(bottom, bottom);
        Assertions.assertTrue(bottom.isEqual(man, stillBottom));

        Abstract1 nowTop = this.widenFixed(bottom, top);
        Assertions.assertTrue(top.isEqual(man, nowTop));
    }

    private Abstract1 widenFixed(Abstract1 oldState, Abstract1 newState) throws ApronException {
        // apron requires explicit joining before widening
        Abstract1 joined = newState.joinCopy(man, oldState);
        Abstract1 widened = oldState.widening(man, joined);
        return widened;
    }

    ////////////////////////////////////
    // Adding variable to environment //
    ////////////////////////////////////
    @Test
    public void testAddVariable() throws ApronException{
        // encode 1 <= x <= 2, -1 <= y <= 1
        Interval[] box = { new Interval(1, 2), new Interval(-1, 1) };
        Abstract1 old_abstr = new Abstract1(man, env, integer_names, box);

        String[] integer_new = { "z" };
        String[] real_new = {};
        Environment new_env = this.env.add(integer_new, real_new);
        String expectedEnv = "( i: { x, y, z }, r: { } )";
        Assertions.assertEquals(expectedEnv, new_env.toString());

        Abstract1 new_0 = old_abstr.changeEnvironmentCopy(man, new_env, false); // sets the new variables to  [-oo,+oo] 
        String expected0 = "{  -1x +2 >= 0;  -1y +1 >= 0;  1y +1 >= 0;  1x -1 >= 0 }";
        Assertions.assertEquals(expected0, new_0.toString());

        Abstract1 new_1 = old_abstr.changeEnvironmentCopy(man, new_env, true); // sets the new variables to 0
        String expected1 = "{  1z = 0;  -1x +2 >= 0;  -1y +1 >= 0;  1y +1 >= 0;  1x -1 >= 0 }";
        Assertions.assertEquals(expected1, new_1.toString());

    }


    //////////////////////////////////////////
    // Remove all constraints on a variable //
    //////////////////////////////////////////
    @Test
    public void testForgetVariable() throws ApronException{
        // encode 1 <= x <= 2, -1 <= y <= 1
        Interval[] box = { new Interval(1, 2), new Interval(-1, 1) };
        Abstract1 old_abstr = new Abstract1(man, env, integer_names, box);

        Abstract1 new_1 = old_abstr.forgetCopy(man, "x", false);
        // x is now set to [-oo, +oo], ie we have no information on it
        String expected1 = "{  -1y +1 >= 0;  1y +1 >= 0 }";
        Assertions.assertEquals(expected1, new_1.toString());

    }

}
