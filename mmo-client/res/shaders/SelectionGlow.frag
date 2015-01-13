varying vec2 texCoord1;
uniform sampler2D m_GlowMap;
uniform vec4 m_Front;
uniform vec4 m_Back;

void main(){
  #ifdef HAS_GLOWMAP
    #if defined(NEED_TEXCOORD1) 
       vec2 crd = texCoord1;
    #else 
       vec2 crd = texCoord;
    #endif
    gl_FragColor = texture2D(m_GlowMap, crd);
    float a = gl_FragColor.a;
    gl_FragColor.rgb = m_Front.rgb * a + m_Back.rgb * (1.0 - a);
  #else
  	gl_FragColor = vec4(0.0);
  #endif
}