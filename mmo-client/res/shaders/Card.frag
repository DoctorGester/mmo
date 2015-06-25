varying vec2 texCoord;

uniform sampler2D m_Base;
uniform sampler2D m_Mask;
uniform sampler2D m_Portrait;
uniform sampler2D m_Content;

void main(){
    vec4 baseColor = texture2D(m_Base, texCoord);
    vec4 maskColor = texture2D(m_Mask, texCoord);
    vec4 portraitColor = texture2D(m_Portrait, texCoord);
    vec4 contentColor = texture2D(m_Content, texCoord);

    float a = min(1.0, portraitColor.a + contentColor.a);

    vec4 overlayColor = vec4(mix(contentColor.rgb, portraitColor.rgb, maskColor.a), 1.0);

    gl_FragColor = vec4(mix(baseColor.rgb, overlayColor.rgb, a), 1.0);
}