package buildcraft.lib.expression;

import net.minecraft.item.EnumDyeColor;

import buildcraft.lib.misc.ColourUtil;

public class DefaultContexts {
    public static final FunctionContext STRINGS = new FunctionContext();
    public static final FunctionContext MATH_SCALAR = new FunctionContext();
    public static final FunctionContext MATH_VECTOR = new FunctionContext();
    public static final FunctionContext RENDERING = new FunctionContext();

    private static final FunctionContext[] CTX_ARRAY_ALL = { STRINGS, MATH_SCALAR, MATH_VECTOR, RENDERING };

    public static final FunctionContext CONTEXT_DEFAULT = new FunctionContext(CTX_ARRAY_ALL);

    static {
        System.out.println(Math.ceil(Math.random() * 4));

        // STRINGS.put_s_s("lowercase", (a) -> a.toLowerCase(Locale.ROOT));
        // STRINGS.put_s_s("uppercase", (a) -> a.toUpperCase(Locale.ROOT));
        STRINGS.put_s_l("length", (a) -> a.length());
        // STRINGS.put_sl_s("string_at", (a, b) -> Character.toString(a.charAt(b)));
        // STRINGS.put_sl_s("substring", (a, b) -> a.substring(b));
        // STRINGS.put_sll_s("substring", (a, b, c) -> a.substring(b, c));
        // STRINGS.put_sll_s("substring_rel", (a, b, c) -> a.substring(b, b + c));

        MATH_SCALAR.putConstantDouble("pi", Math.PI);
        MATH_SCALAR.putConstantDouble("e", Math.E);

        MATH_SCALAR.put_l_l("abs_long", (a) -> Math.abs(a));
        MATH_SCALAR.put_d_d("abs_double", (a) -> Math.abs(a));

        MATH_SCALAR.put_d_l("round", (a) -> Math.round(a));
        MATH_SCALAR.put_d_l("floor", (a) -> (long) Math.floor(a));
        MATH_SCALAR.put_d_l("ceil", (a) -> (long) Math.ceil(a));
        MATH_SCALAR.put_d_l("sign", (a) -> a == 0 ? 0 : a < 0 ? -1 : 1);

        MATH_SCALAR.put_d_d("log", (a) -> Math.log(a));
        MATH_SCALAR.put_d_d("log10", (a) -> Math.log10(a));
        MATH_SCALAR.put_d_d("sqrt", (a) -> Math.sqrt(a));
        MATH_SCALAR.put_d_d("cbrt", (a) -> Math.cbrt(a));

        MATH_SCALAR.put_d_d("degrees", (a) -> Math.toDegrees(a));
        MATH_SCALAR.put_d_d("radians", (a) -> Math.toRadians(a));
        MATH_SCALAR.put_d_d("round_float", (a) -> Math.round(a * 1e10) / 1e10);

        MATH_SCALAR.put_d_d("sin", (a) -> Math.sin(a));
        MATH_SCALAR.put_d_d("cos", (a) -> Math.cos(a));
        MATH_SCALAR.put_d_d("tan", (a) -> Math.tan(a));

        MATH_SCALAR.put_d_d("asin", (a) -> Math.asin(a));
        MATH_SCALAR.put_d_d("acos", (a) -> Math.acos(a));
        MATH_SCALAR.put_d_d("atan", (a) -> Math.atan(a));
        MATH_SCALAR.put_dd_d("atan2", (a, b) -> Math.atan2(a, b));

        MATH_SCALAR.put_d_d("sinh", (a) -> Math.sinh(a));
        MATH_SCALAR.put_d_d("cosh", (a) -> Math.cosh(a));
        MATH_SCALAR.put_d_d("tanh", (a) -> Math.tanh(a));

        MATH_SCALAR.put_ll_l("min_long", (a, b) -> Math.min(a, b));
        MATH_SCALAR.put_ll_l("max_long", (a, b) -> Math.max(a, b));
        MATH_SCALAR.put_dd_d("min_double", (a, b) -> Math.min(a, b));
        MATH_SCALAR.put_dd_d("max_double", (a, b) -> Math.max(a, b));

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

        RENDERING.put_s_l("convertColourToAbgr", DefaultContexts::convertColourToAbgr);
        RENDERING.put_s_l("convertColourToArgb", DefaultContexts::convertColourToArgb);
    }

    private static long convertColourToAbgr(String c) {
        EnumDyeColor colour = ColourUtil.parseColourOrNull(c);
        if (colour == null) return 0xFF_FF_FF_FF;
        return 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(colour));
    }

    private static long convertColourToArgb(String c) {
        EnumDyeColor colour = ColourUtil.parseColourOrNull(c);
        if (colour == null) return 0xFF_FF_FF_FF;
        return 0xFF_00_00_00 | ColourUtil.getLightHex(colour);
    }

    public static FunctionContext createWithAll() {
        return new FunctionContext(CTX_ARRAY_ALL);
    }
}
