#if defined(HAS_GLOWMAP) || defined(HAS_COLORMAP) || (defined(HAS_LIGHTMAP) && !defined(SEPARATE_TEXCOORD))
    #define NEED_TEXCOORD1
#endif

#if defined(DISCARD_ALPHA)
    uniform float m_AlphaDiscardThreshold;
#endif

#define M_PI 3.1415926535897932384626433832795

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

uniform float m_Value;
uniform float m_MaxValue;
uniform float m_ValuePerBar;

varying vec2 texCoord1;
varying vec2 texCoord2;

varying vec4 vertColor;

// TODO remove branching
void main(){
    vec4 color = m_Color;

    float amount = m_MaxValue / m_ValuePerBar;
    const float width = 0.002;

    //color.rgb = color.rgb * cos(texCoord1.x);

    if (abs(cos(texCoord1.x * M_PI * amount)) > 1.0 - (width * amount)){
        discard;
    } else if (texCoord1.x > m_Value / m_MaxValue) {
        color.rgb = vec3(0.55);
    }

    #if defined(DISCARD_ALPHA)
        if(color.a < m_AlphaDiscardThreshold){
           discard;
        }
    #endif

    gl_FragColor.rgba = color;
}