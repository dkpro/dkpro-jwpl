#/bin/sh

# Formatter settings
JDT_CORE_PREFS="dkpro-jwpl-build/src/main/resources/dkpro-jwpl/eclipse/org.eclipse.jdt.core.prefs"

# Save actions
JDT_UI_PREFS="dkpro-jwpl-build/src/main/resources/dkpro-jwpl/eclipse/org.eclipse.jdt.ui.prefs"

function installPrefs {
  mkdir -p $1/.settings/
  cp -v $JDT_CORE_PREFS $1/.settings/
  cp -v $JDT_UI_PREFS $1/.settings/
}

installPrefs dkpro-jwpl-api
installPrefs dkpro-jwpl-datamachine
installPrefs dkpro-jwpl-deps
installPrefs dkpro-jwpl-mwdumper
installPrefs dkpro-jwpl-parser
installPrefs dkpro-jwpl-revisionmachine
installPrefs dkpro-jwpl-timemachine
installPrefs dkpro-jwpl-tutorial
installPrefs dkpro-jwpl-util
installPrefs dkpro-jwpl-wikimachine
