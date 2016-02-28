#define AppName "Qabel Desktop Client"
#define AppVersion "0.1"
#define AppPublisher "Qabel GmbH"
#define AppURL "https://qabel.de"
#define AppExeName "desktopLaunch4j.exe"
#define SetupIcon "logo.ico"

[Setup]
AppId={{2271156D-AF5F-491E-B9D0-FE78708D1464}
AppName={#AppName}
AppVersion={#AppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppURL}
AppSupportURL={#AppURL}
AppUpdatesURL={#AppURL}
DefaultDirName={pf}\{#AppName}
DefaultGroupName={#AppName}
AllowNoIcons=yes
LicenseFile=..\LICENSE
OutputDir=.
OutputBaseFilename=QabelSetup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile={#SetupIcon}
UninstallIconFile={#SetupIcon}
UsePreviousAppDir=True
AppCopyright={#AppPublisher}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "desktopLaunch4j.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExeName}"
Name: "{group}\{cm:UninstallProgram,{#AppName}}"; Filename: "{uninstallexe}"    
Name: "{commondesktop}\{#AppName}"; Filename: "{app}\{#AppExeName}"; Tasks: desktopicon
Name: "{userstartup}\{#AppName}"; Filename: "{app}\{#AppExeName}"

[Run]
Filename: "{app}\{#AppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(AppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
