#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 v_TexCoord;
uniform samplerExternalOES sTexture;
uniform vec2 texSize;
uniform vec3 keyColor;
uniform float threshold;

//bool isBackground(vec3 color) {
//    float red = 1.0;
//    float green = 1.0;
//    float blue = 1.0;
//    float accuracy = 0.3;
//    if (abs(color.r - red) <= accuracy && abs(color.g - green) <= accuracy && abs(color.b - blue) <= accuracy) {
//        return true;
//    }
//    return false;
//}

//const vec3 keyColor = vec3(0.0, 1.0, 0.0); // Green screen color
//const float threshold = 0.7; // Threshold to determine the range of colors to consider transparent
const float smoothness = 0.05; // Controls the smoothness of the edges


void main() {
    vec4 color = texture2D(sTexture, v_TexCoord);
    float keyDifference = length(color.rgb - keyColor);
    float alpha = smoothstep(threshold - smoothness, threshold + smoothness, keyDifference);
    gl_FragColor = vec4(color.rgb, alpha);


    //    if (isBackground(color.rgb)) {
//        gl_FragColor = vec4(color.r, color.g, color.b, 0.0);
//    } else {
////        vec3 data = vec3(0.0, 0.0, 0.0);
////
////        float sigma_gauss = 5.0;
////        int radius = 4;
////        float total_weight = 0.0;
////        for (int x = -radius; x <= radius; ++x) {
////            for (int y = -radius; y <= radius; ++y) {
//////                vec3 tex = texture2D(sTexture, v_TexCoord + vec2(float(x) / float(texSize.x), float(y) / float(texSize.y))).rgb;
////                vec3 tex = texture2D(sTexture, v_TexCoord + vec2(float(x) / 722.0, float(y) / 1280.0)).rgb;
////                if (isBackground(tex)) {
////                    continue;
////                }
////                float weight = exp(-(float(x * x + y * y))/(sigma_gauss * sigma_gauss));
////                data += tex*weight;
////                //                data += texture(shadow_map, shadow_pos.xy + vec2(x, y) / shadow_map_resolution).rg * weight;
////                total_weight += weight;
////            }
////        }
////        data /= total_weight;
////        gl_FragColor = vec4(data, 1.0);
//        gl_FragColor = vec4(color.r, color.g, color.b, 1.0);
//    }
}
