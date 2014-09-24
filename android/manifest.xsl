<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:android="http://schemas.android.com/apk/res/android">


    <!-- debugging -->
    <xsl:param name="GServicesDebug"></xsl:param>

    <xsl:template match="meta-data[@android:name='GServicesDebug']">
        <meta-data android:name="GServicesDebug" android:value="{$GServicesDebug}"/>
    </xsl:template>
    

    <!-- Admob Variables -->
    
    <xsl:param name="useAdmob"></xsl:param>

    <xsl:template match="meta-data[@android:name='useAdmob']">
        <meta-data android:name="useAdmob" android:value="{$useAdmob}"/>
    </xsl:template>
    
        
    <xsl:param name="admobType"></xsl:param>

    <xsl:template match="meta-data[@android:name='admobType']">
        <meta-data android:name="admobType" android:value="{$admobType}"/>
    </xsl:template>
    
    <xsl:param name="admobUnitID"></xsl:param>

    <xsl:template match="meta-data[@android:name='admobUnitID']">
        <meta-data android:name="admobUnitID" android:value="{$admobUnitID}"/>
    </xsl:template>
    
    <xsl:param name="testDeviceID"></xsl:param>

    <xsl:template match="meta-data[@android:name='testDeviceID']">
        <meta-data android:name="testDeviceID" android:value="{$testDeviceID}"/>
    </xsl:template>
    
    <!-- Google Play variables -->
    
    <xsl:param name="useGooglePlay"></xsl:param>

    <xsl:template match="meta-data[@android:name='useGooglePlay']">
        <meta-data android:name="useGooglePlay" android:value="{$useGooglePlay}"/>
    </xsl:template>
    
    <xsl:param name="GooglePlayID" />
    
    <xsl:template match="meta-data[@android:name='com.google.android.gms.appstate.APP_ID']">
		<meta-data android:name="com.google.android.gms.appstate.APP_ID" android:value="\ {$GooglePlayID}" />
	</xsl:template>
	<xsl:template match="meta-data[@android:name='com.google.android.gms.games.APP_ID']">
		<meta-data android:name="com.google.android.gms.games.APP_ID" android:value="\ {$GooglePlayID}" />
	</xsl:template>

    <!--    <xsl:strip-space elements="*" />-->
    <xsl:output indent="yes" />

    <xsl:template match="comment()" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>