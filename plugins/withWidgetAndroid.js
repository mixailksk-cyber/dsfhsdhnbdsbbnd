const {
  createRunOncePlugin,
  withAndroidManifest,
  withDangerousMod,
} = require('@expo/config-plugins');
const path = require('path');
const fs = require('fs');

const withWidgetAndroid = (config) => {
  // Добавляем ресивер в AndroidManifest.xml
  config = withAndroidManifest(config, (config) => {
    const mainApplication = config.modResults.manifest.application[0];
    
    if (!mainApplication.receiver) {
      mainApplication.receiver = [];
    }
    
    // Проверяем, не добавлен ли уже ресивер
    const hasWidgetReceiver = mainApplication.receiver.some(
      receiver => receiver.$ && receiver.$['android:name'] === '.widget.NotesWidget'
    );
    
    if (!hasWidgetReceiver) {
      mainApplication.receiver.push({
        $: {
          'android:name': '.widget.NotesWidget',
          'android:exported': 'true' // КРИТИЧЕСКИ ВАЖНО!
        },
        'intent-filter': [{
          action: [{
            $: {
              'android:name': 'android.appwidget.action.APPWIDGET_UPDATE'
            }
          }]
        }],
        'meta-data': [{
          $: {
            'android:name': 'android.appwidget.provider',
            'android:resource': '@xml/notes_widget_info'
          }
        }]
      });
    }
    
    return config;
  });

  // Копируем файлы виджета
  config = withDangerousMod(config, [
    'android',
    async (config) => {
      const platformRoot = path.join(config.modRequest.platformProjectRoot, 'app/src/main');
      const packagePath = 'com/mkhailksk/snack5055ac3e1432423490bec9b4cbbab3f9';
      
      // Копируем Java/Kotlin файлы
      const widgetSource = path.join(config.modRequest.projectRoot, 'widgets/android/src/main/java');
      const targetSource = path.join(platformRoot, 'java', packagePath);
      
      if (fs.existsSync(widgetSource)) {
        copyFolderRecursive(widgetSource, targetSource);
        console.log('✓ Widget Java files copied');
      } else {
        console.warn('⚠ Widget source folder not found:', widgetSource);
        // Создаем структуру папок
        fs.mkdirSync(path.join(targetSource, 'widget'), { recursive: true });
      }
      
      // Копируем ресурсы
      const widgetRes = path.join(config.modRequest.projectRoot, 'widgets/android/src/main/res');
      const targetRes = path.join(platformRoot, 'res');
      
      if (fs.existsSync(widgetRes)) {
        copyFolderRecursive(widgetRes, targetRes);
        console.log('✓ Widget resource files copied');
      }
      
      return config;
    }
  ]);

  return config;
};

function copyFolderRecursive(source, target) {
  if (!fs.existsSync(target)) {
    fs.mkdirSync(target, { recursive: true });
  }
  
  const files = fs.readdirSync(source);
  
  files.forEach(file => {
    const sourcePath = path.join(source, file);
    const targetPath = path.join(target, file);
    
    if (fs.lstatSync(sourcePath).isDirectory()) {
      copyFolderRecursive(sourcePath, targetPath);
    } else {
      fs.copyFileSync(sourcePath, targetPath);
      console.log(`  Copied: ${file}`);
    }
  });
}

module.exports = createRunOncePlugin(withWidgetAndroid, 'withWidgetAndroid', '1.0.0');