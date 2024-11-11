<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- These are Apache FOP representations of the 'ruling_template' CSS styles -->

  <xsl:attribute-set name="print-document">
    <!-- padding-* removed -->
  </xsl:attribute-set>

  <xsl:attribute-set name="prevent-content-split">
    <xsl:attribute name="keep-together.within-page">1</xsl:attribute>
    <!-- Changed page-break-inside to keep-together.within-page for long text segments -->
  </xsl:attribute-set>

  <xsl:attribute-set name="organisation-logo__container">
    <xsl:attribute name="margin-left">8px</xsl:attribute>
    <xsl:attribute name="border-left">2px solid #009390</xsl:attribute>
    <xsl:attribute name="margin-bottom">12px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="organisation-logo__crest-hmrc">
    <xsl:attribute name="margin-left">8px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="organisation-logo__name">
    <xsl:attribute name="font-family">Helvetica,GDS Transport,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">400</xsl:attribute>
    <xsl:attribute name="font-size">1.3em</xsl:attribute>
    <xsl:attribute name="margin-left">8px</xsl:attribute>
    <xsl:attribute name="margin-right">50px</xsl:attribute>
    <xsl:attribute name="width">70px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="print-header">
    <xsl:attribute name="color">#00703C</xsl:attribute>
    <xsl:attribute name="font-family">GDS Transport,Arial,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">700</xsl:attribute>
    <xsl:attribute name="font-size">1.2em</xsl:attribute>
    <xsl:attribute name="text-align">right</xsl:attribute>
    <xsl:attribute name="margin-top">5px</xsl:attribute>
    <xsl:attribute name="margin-left">0px</xsl:attribute>
    <xsl:attribute name="margin-right">15px</xsl:attribute>
    <xsl:attribute name="margin-bottom">0px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="ruling-reference">
    <xsl:attribute name="color">#505a5f</xsl:attribute>
    <xsl:attribute name="font-family">GDS Transport,Arial,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">400</xsl:attribute>
    <xsl:attribute name="font-size">0.9em</xsl:attribute>
    <xsl:attribute name="line-height">1.11111</xsl:attribute>
    <xsl:attribute name="margin-top">0px</xsl:attribute>
    <xsl:attribute name="margin-bottom">10px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="heading-large">
    <xsl:attribute name="color">#0b0c0c</xsl:attribute>
    <xsl:attribute name="font-family">GDS Transport,Arial,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">700</xsl:attribute>
    <xsl:attribute name="font-size">1.7em</xsl:attribute>
    <xsl:attribute name="line-height">1.11111</xsl:attribute>
    <xsl:attribute name="margin-top">10px</xsl:attribute>
    <xsl:attribute name="margin-bottom">10px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="govuk-tag__grey">
    <xsl:attribute name="color">#454a4d</xsl:attribute>
    <xsl:attribute name="background-color">#eff0f1</xsl:attribute>
    <xsl:attribute name="font-family">GDS Transport,Arial,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-size">0.86em</xsl:attribute>
    <xsl:attribute name="line-height">1.25</xsl:attribute>
    <xsl:attribute name="text-transform">uppercase</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="details">
    <xsl:attribute name="font-family">GDS Transport,Arial,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="font-size">0.86em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="details-info">
    <xsl:attribute name="font-family">GDS Transport,Arial,sans-serif</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-size">0.86em</xsl:attribute>
  </xsl:attribute-set>
</xsl:stylesheet>
