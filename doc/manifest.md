
---

### 🔑 Estrutura Geral

* `<manifest ...>` → raiz do documento. Define namespaces (android, tools).
* `<uses-permission>` → permissões que o app precisa (internet, câmera, localização, etc).
* `<application>` → configurações globais do app (tema, ícone, classes principais).
* `<activity>`, `<service>`, `<receiver>` → componentes que o app expõe ou usa.
* `<meta-data>` → metadados extras para o Android ou bibliotecas.

---

### 📌 Permissões Declaradas

1. **Básicas de rede:**

   * `INTERNET`: permite o app acessar a internet.
   * `ACCESS_NETWORK_STATE`: permite checar se há conexão ativa.

2. **Armazenamento (legado, com maxSdkVersion):**

   * `READ_EXTERNAL_STORAGE` até Android 12 (API 32).
   * `WRITE_EXTERNAL_STORAGE` até Android 9 (API 28).

3. **Localização e sensores:**

   * `ACCESS_COARSE_LOCATION` e `ACCESS_FINE_LOCATION`: localização aproximada e precisa.
   * `CAMERA`: acesso à câmera.
   * `RECORD_AUDIO`: acesso ao microfone.

4. **Outros:**

   * `INSTALL_SHORTCUT`: criar atalhos na tela inicial.
   * `VIBRATE`: controlar vibração.
   * `USE_BIOMETRIC`: usar biometria.
   * `REQUEST_INSTALL_PACKAGES`: instalar APKs baixados.
   * `QUERY_ALL_PACKAGES`: listar todos apps instalados.
   * `POST_NOTIFICATIONS`: enviar notificações (Android 13+).
   * `READ_MEDIA_AUDIO`: acessar arquivos de mídia (Android 13+).
   * `CREDENTIAL_MANAGER_*`: permissões novas para gerenciar credenciais (Android 14+).

---

### 📌 Features

* `<uses-feature android:name="android.hardware.camera" android:required="false" />`
* Isso evita que a Play Store restrinja a instalação apenas a celulares com câmera.

---

### 📌 Aplicativo (`<application>`)

* Classe principal: `.BrowserApplication`.
* Backup desativado (`allowBackup="false"`).
* Ícones definidos (`@mipmap/ic_launcher` etc).
* Tema padrão: `@style/NormalTheme`.
* `usesCleartextTraffic="true"` → permite HTTP sem HTTPS (útil para desenvolvimento).

---

### 📌 Firebase e Crashlytics

* Vários `<meta-data>` desativam coleta automática de analytics e mensageria do Firebase.
* Ou seja, a telemetria vem desligada por padrão.

---

### 📌 Profileable

* `<profileable android:shell="true" />` permite que ferramentas de perfilamento monitorem o app (debug, tracing).

---

### 📌 Activities e Aliases

O app é **um navegador** (parece um fork de Mozilla Firefox/Fennec).
Tem vários `activity-alias`, que funcionam como **atalhos alternativos de entrada** para o app.

* `${applicationId}.App`: ponto de entrada principal, aparece no launcher.
* `${applicationId}.AlternativeApp`: versão alternativa do ícone (desativada por padrão).
* `org.mozilla.gecko.BrowserApp` e `org.mozilla.gecko.LauncherActivity`: compatibilidade com versões antigas (Fennec).

#### Atividade principal:

* `.HomeActivity`: onde o app abre normalmente.
* Tem **deep links** configurados com `android:scheme="${deepLinkScheme}"` (provavelmente "moz" ou algo parecido).
  Exemplos: `moz://home`, `moz://settings`, etc.

#### `.IntentReceiverActivity`:

* Configurada para abrir:

  * Navegação web (`http`, `https`).
  * Pesquisas (`SEARCH`, `WEB_SEARCH`).
  * PWAs (`VIEW_PWA`).
  * NFC (`NDEF_DISCOVERED`).
  * Arquivos PDF (`application/pdf`).
  * Assistente virtual (`ACTION_ASSIST`).
* Serve como **"central de intents"** do navegador.

---

### 📌 Outras Atividades

* `.VoiceSearchActivity`: busca por voz.
* `.AuthCustomTabActivity` e `.AuthIntentReceiverActivity`: autenticação de conta.
* `.Autofill*Activity`: janelas relacionadas ao **autofill** (preenchimento automático).
* `.NotificationClickedReceiverActivity`: interage com notificações.

---

### 📌 Services

* `.autofill.AutofillService`: serviço de preenchimento automático (API 26+).
* `.media.MediaSessionService`: gerencia sessões de mídia (play/pause, notificações).
* `.customtabs.CustomTabsService`: suporte a **Custom Tabs** (abas personalizadas).
* `.downloads.DownloadService`: gerencia downloads.
* `.session.PrivateNotificationService`: lida com notificações de **abas privadas**.
* `.messaging.NotificationDismissedService`: trata notificações descartadas.

---

### 📌 Broadcast Receivers

* `.onboarding.WidgetPinnedReceiver`: recebe evento quando widget é fixado na tela inicial.

---

### 📌 Provider

* Remove inicialização automática do **WorkManager** (usando `tools:node="remove"`).
  Isso indica que o app inicializa o WorkManager de forma **manual/on-demand**.

---

### 🔎 Resumindo:

Esse **manifesto** é de um navegador baseado em **Firefox/Fennec** (provavelmente um fork, como o **Fennec F-Droid**).

Ele:

* Pede permissões de navegador (rede, armazenamento, câmera, microfone).
* Define atividades principais (`HomeActivity` e `IntentReceiverActivity`) que lidam com **deep links, intents de pesquisa, PWA, PDFs, NFC, notificações**.
* Usa **services** para mídia, downloads, autofill e notificações.
* Configura **compatibilidade** com antigas versões do Firefox/Fennec.
* Desativa por padrão a telemetria do Firebase.
* Dá suporte a **atalhos, widgets, voice search, biometria e credential manager**.

---

