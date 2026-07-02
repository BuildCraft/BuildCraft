/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.value.NodeVariableDouble;

public class DefaultContexts {
    public static final FunctionContext MATH_SCALAR = new FunctionContext("Math: Scalar");
    public static final FunctionContext MATH_VECTOR = new FunctionContext("Math: Vector", NodeTypes.VEC_LONG, NodeTypes.VEC_DOUBLE);
    public static final FunctionContext RENDERING = new FunctionContext("Rendering");
    public static final FunctionContext LOCALIZATION = new FunctionContext("Localization");
    public static final FunctionContext CONFIG = new FunctionContext("Config");

    public static final NodeVariableDouble RENDER_PARTIAL_TICKS;

    private static final FunctionContext[] CTX_ARRAY_ALL = { NodeTypes.STRING, MATH_SCALAR, MATH_VECTOR, RENDERING, LOCALIZATION };

    /** Creates a new {@link FunctionContext} with all of the functions given in this class. */
    public static FunctionContext createWithAll() {
        return createWithAll("all");
    }

    public static FunctionContext createWithAll(String name) {
        return new FunctionContext(name, CTX_ARRAY_ALL);
    }

    static {
        MATH_SCALAR.putConstantDouble("pi", Math.PI);
        MATH_SCALAR.putConstantDouble("e", Math.E);

        MATH_SCALAR.put_l_l("abs", Math::abs, a -> "abs( " + a + ")");
        MATH_SCALAR.put_d_d("abs", Math::abs, a -> "abs( " + a + ")");

        MATH_SCALAR.put_d_l("round", Math::round, a -> "round( " + a + ")");
        MATH_SCALAR.put_d_l("floor", (a) -> (long) Math.floor(a), a -> "floor( " + a + ")");
        MATH_SCALAR.put_d_l("ceil", (a) -> (long) Math.ceil(a), a -> "ceil( " + a + ")");
        MATH_SCALAR.put_d_l("sign", (a) -> a == 0 ? 0 : a < 0 ? -1 : 1, a -> "sign( " + a + ")");

        MATH_SCALAR.put_d_d("log", Math::log);
        MATH_SCALAR.put_d_d("log10", Math::log10);
        MATH_SCALAR.put_d_d("sqrt", Math::sqrt);
        MATH_SCALAR.put_d_d("cbrt", Math::cbrt);

        MATH_SCALAR.put_d_d("degrees", Math::toDegrees);
        MATH_SCALAR.put_d_d("radians", Math::toRadians);
        MATH_SCALAR.put_d_d("round_float", (a) -> Math.round(a * 1e10) / 1e10);

        MATH_SCALAR.put_d_d("sin", Math::sin);
        MATH_SCALAR.put_d_d("cos", Math::cos);
        MATH_SCALAR.put_d_d("tan", Math::tan);

        MATH_SCALAR.put_d_d("asin", Math::asin);
        MATH_SCALAR.put_d_d("acos", Math::acos);
        MATH_SCALAR.put_d_d("atan", Math::atan);
        MATH_SCALAR.put_dd_d("atan2", Math::atan2);

        MATH_SCALAR.put_d_d("sinh", Math::sinh);
        MATH_SCALAR.put_d_d("cosh", Math::cosh);
        MATH_SCALAR.put_d_d("tanh", Math::tanh);

        MATH_SCALAR.put_ll_l("min", Math::min);
        MATH_SCALAR.put_ll_l("max", Math::max);
        MATH_SCALAR.put_dd_d("min", Math::min);
        MATH_SCALAR.put_dd_d("max", Math::max);
        MATH_SCALAR.put_dd_d("pow", Math::pow);

        MATH_SCALAR.put_ddd_d("clamp", (c, min, max) -> Math.max(Math.min(c, max), min));
        MATH_SCALAR.put_lll_l("clamp", (c, min, max) -> Math.max(Math.min(c, max), min));

        // MATH_VECTOR.putConstantVecLong("origin", VecLong.ZERO);
        // MATH_VECTOR.putConstantVecLong("vec_zero", VecLong.ZERO);

        // MATH_VECTOR.put_ll_vl("vec_long", (a, b) -> new VecLong(a, b, 0, 0));
        // MATH_VECTOR.put_lll_vl("vec_long", (a, b, c) -> new VecLong(a, b, c, 0));
        // MATH_VECTOR.put_llll_vl("vec_long", (a, b, c, d) -> new VecLong(a, b, c, d));
        //
        // MATH_VECTOR.put_dd_vd("vec_double", (a, b) -> new VecDouble(a, b, 0, 0));
        // MATH_VECTOR.put_ddd_vd("vec_double", (a, b, c) -> new VecDouble(a, b, c, 0));
        // MATH_VECTOR.put_dddd_vd("vec_double", (a, b, c, d) -> new VecDouble(a, b, c, d));
        //
        // MATH_VECTOR.put_vl_l("x_long", (a) -> a.a);
        // MATH_VECTOR.put_vl_l("y_long", (a) -> a.b);
        // MATH_VECTOR.put_vl_l("z_long", (a) -> a.c);
        // MATH_VECTOR.put_vl_l("w_long", (a) -> a.d);
        // MATH_VECTOR.put_vl_d("length_long", (a) -> a.length());
        //
        // MATH_VECTOR.put_vlvl_l("dot2_long", (a, b) -> a.dotProduct2(b));
        // MATH_VECTOR.put_vlvl_l("dot3_long", (a, b) -> a.dotProduct3(b));
        // MATH_VECTOR.put_vlvl_l("dot4_long", (a, b) -> a.dotProduct4(b));
        //
        // MATH_VECTOR.put_vd_d("x_double", (a) -> a.a);
        // MATH_VECTOR.put_vd_d("y_double", (a) -> a.b);
        // MATH_VECTOR.put_vd_d("z_double", (a) -> a.c);
        // MATH_VECTOR.put_vd_d("w_double", (a) -> a.d);
        // MATH_VECTOR.put_vd_d("length_double", (a) -> a.length());
        //
        // MATH_VECTOR.put_vdvd_d("dot2_double", (a) -> a.dotProduct2(b));
        // MATH_VECTOR.put_vdvd_d("dot3_double", (a) -> a.dotProduct3(b));
        // MATH_VECTOR.put_vdvd_d("dot4_double", (a) -> a.dotProduct4(b));
        //
        // MATH_VECTOR.put_vlvl_d("vec_dist_long", (a, b) -> a.distance(b));
        // MATH_VECTOR.put_vdvd_d("vec_dist_double", (a, b) -> a.distance(b));
        //
        // MATH_VECTOR.put_vlvl_vl("add_long", (a, b) -> a.add(b));
        // MATH_VECTOR.put_vlvl_vl("sub_long", (a, b) -> a.add(b));
        // MATH_VECTOR.put_vlvl_vl("scale_long", (a, b) -> a.add(b));
        // MATH_VECTOR.put_vlvl_vl("div_long", (a, b) -> a.add(b));
        //
        // MATH_VECTOR.put_vdvd_vd("add_double", (a, b) -> a.add(b));
        // MATH_VECTOR.put_vdvd_vd("sub_double", (a, b) -> a.add(b));
        // MATH_VECTOR.put_vdvd_vd("scale_double", (a, b) -> a.add(b));
        // MATH_VECTOR.put_vdvd_vd("div_double", (a, b) -> a.add(b));
        //
        // MATH_VECTOR.put_vll_vl("add", (a, b) -> a.add(b, 0, 0, 0));
        // MATH_VECTOR.put_vlll_vl("add", (a, b, c) -> a.add(b, c, 0, 0));
        // MATH_VECTOR.put_vllll_vl("add", (a, b, c, d) -> a.add(b, c, d, 0));
        // MATH_VECTOR.put_vlllll_vl("add", (a, b, c, d, e) -> a.add(b, c, d, e));
        //
        // MATH_VECTOR.put_vll_vl("sub", (a, b) -> a.sub(b, 0, 0, 0));
        // MATH_VECTOR.put_vlll_vl("sub", (a, b, c) -> a.sub(b, c, 0, 0));
        // MATH_VECTOR.put_vllll_vl("sub", (a, b, c, d) -> a.sub(b, c, d, 0));
        // MATH_VECTOR.put_vlllll_vl("sub", (a, b, c, d, e) -> a.sub(b, c, d, e));
        //
        // MATH_VECTOR.put_vll_vl("scale", (a, b) -> a.scale(b, 0, 0, 0));
        // MATH_VECTOR.put_vlll_vl("scale", (a, b, c) -> a.scale(b, c, 0, 0));
        // MATH_VECTOR.put_vllll_vl("scale", (a, b, c, d) -> a.scale(b, c, d, 0));
        // MATH_VECTOR.put_vlllll_vl("scale", (a, b, c, d, e) -> a.scale(b, c, d, e));
        //
        // MATH_VECTOR.put_vll_vl("div", (a, b) -> a.div(b, 0, 0, 0));
        // MATH_VECTOR.put_vlll_vl("div", (a, b, c) -> a.div(b, c, 0, 0));
        // MATH_VECTOR.put_vllll_vl("div", (a, b, c, d) -> a.div(b, c, d, 0));
        // MATH_VECTOR.put_vlllll_vl("div", (a, b, c, d, e) -> a.div(b, c, d, e));
        //
        // MATH_VECTOR.put_vdd_vd("add", (a, b) -> a.add(b, 0, 0, 0));
        // MATH_VECTOR.put_vddd_vd("add", (a, b, c) -> a.add(b, c, 0, 0));
        // MATH_VECTOR.put_vdddd_vd("add", (a, b, c, d) -> a.add(b, c, d, 0));
        // MATH_VECTOR.put_vddddd_vd("add", (a, b, c, d, e) -> a.add(b, c, d, e));
        //
        // MATH_VECTOR.put_vdd_vd("sub", (a, b) -> a.sub(b, 0, 0, 0));
        // MATH_VECTOR.put_vddd_vd("sub", (a, b, c) -> a.sub(b, c, 0, 0));
        // MATH_VECTOR.put_vdddd_vd("sub", (a, b, c, d) -> a.sub(b, c, d, 0));
        // MATH_VECTOR.put_vddddd_vd("sub", (a, b, c, d, e) -> a.sub(b, c, d, e));
        //
        // MATH_VECTOR.put_vdd_vd("scale", (a, b) -> a.scale(b, 0, 0, 0));
        // MATH_VECTOR.put_vddd_vd("scale", (a, b, c) -> a.scale(b, c, 0, 0));
        // MATH_VECTOR.put_vdddd_vd("scale", (a, b, c, d) -> a.scale(b, c, d, 0));
        // MATH_VECTOR.put_vddddd_vd("scale", (a, b, c, d, e) -> a.scale(b, c, d, e));
        //
        // MATH_VECTOR.put_vdd_vd("div", (a, b) -> a.div(b, 0, 0, 0));
        // MATH_VECTOR.put_vddd_vd("div", (a, b, c) -> a.div(b, c, 0, 0));
        // MATH_VECTOR.put_vdddd_vd("div", (a, b, c, d) -> a.div(b, c, d, 0));
        // MATH_VECTOR.put_vddddd_vd("div", (a, b, c, d, e) -> a.div(b, c, d, e));

        RENDER_PARTIAL_TICKS = RENDERING.putVariableDouble("partial_ticks");
    }
}
