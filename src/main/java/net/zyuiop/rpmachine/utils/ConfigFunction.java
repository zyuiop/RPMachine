package net.zyuiop.rpmachine.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.function.DoubleFunction;
import java.util.function.Function;

/**
 * @author Louis Vialar
 */
public enum ConfigFunction {
    LINEAR(c -> {
        double p0 = c.getDouble("p0");
        double coeff = c.getDouble("coeff");
        return x -> ((p0 + coeff * x));
    }),
    EXPONENTIAL(c -> {
        double p0 = c.getDouble("p0");
        double basis = c.getDouble("basis");
        return x -> ((p0 * Math.pow(basis, x)));
    }),
    SLOW_EXPONENTIAL(c -> {
        double p0 = c.getDouble("p0");
        double basis = c.getDouble("basis");
        return x -> ((p0 * Math.pow(basis, Math.sqrt(x))));
    }),
    QUADRATIC(c -> {
        double p0 = c.getDouble("p0");
        double power = c.getDouble("power");
        return x -> ((p0 * Math.pow(x + 1, power)));
    }),
    SIGMOID(c -> {
        double ampl = c.getDouble("max");
        double delta = c.getDouble("delta");
        double coeff = c.getDouble("coeff");
        return x -> ((ampl / (1 + Math.exp(-coeff * (x - delta)))));
    }),
    FIXED(c -> {
        double price = c.getDouble("value");
        return x -> price;
    }),
    BOUNDED(c -> {
        double min = c.getDouble("min");
        double max = c.getDouble("max");
        DoubleFunction<Double> base = getFunction(c.getConfigurationSection("of"), null);
        return x -> Math.max(min, Math.min(max, base.apply(x)));
    });

    private final Function<ConfigurationSection, DoubleFunction<Double>> creator;

    ConfigFunction(Function<ConfigurationSection, DoubleFunction<Double>> creator) {
        this.creator = creator;
    }

    public DoubleFunction<Double> function(ConfigurationSection s) {
        return creator.apply(s);
    }

    public static DoubleFunction<Double> getFunction(ConfigurationSection s, DoubleFunction<Double> defaultFunc) {
        try {
            return valueOf(s.getString("function").toUpperCase()).function(s);
        } catch (NullPointerException | IllegalArgumentException e) {
            return defaultFunc;
        }
    }
}