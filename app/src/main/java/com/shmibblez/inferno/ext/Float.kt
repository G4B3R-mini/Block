package com.shmibblez.inferno.ext

fun Float.toRange(from: Pair<Float, Float>, to: Pair<Float, Float>): Float {
    // stolen from: https://stackoverflow.com/a/43045004/11591403
//    (x - input_start) / (input_end - input_start) * (output_end - output_start) + output_start
    return (this - from.first) / (from.second - from.first) * (to.second - to.first) + to.first
}