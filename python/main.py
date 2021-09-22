import os
import xml.etree.ElementTree as ET
import platform
from enum import Enum


class SystemName(Enum):
    WINDOWS = 'Windows'
    MAC = 'Darwin'
    LINUX = 'Linux'


def get_system_name():
    return platform.system()


if __name__ == '__main__':
    user_profile_path = f'{os.environ["APPDATA"]}\\JetBrains'
    
    for dirs in os.listdir(user_profile_path):
        for app in ['IntelliJ', 'PyCharm', 'Rider', 'WebStorm', 'PhpStorm', 'GoLand']:
            if app in dirs:
                app_dir_path = f'{user_profile_path}\\{dirs}'
    
                # Delete eval folder with licence key
                eval_dir_path = f'{app_dir_path}\\eval'
                for f in os.listdir(eval_dir_path):
                    os.remove(os.path.join(eval_dir_path, f))
    
                # Update options.xml
                options_file_path = f'{app_dir_path}\\options\\other.xml'
    
                try:
                    tree = ET.parse(options_file_path)
                    root = tree.getroot()
                    x = root.find('.//property/..')
                    for e in root.findall('.//property'):
                        if 'evlsprt' in e.attrib['name']:
                            x.remove(e)
                    tree.write(options_file_path)
                except Exception as e:
                    print(e)
    
    # Delete registry key
    os.system('cmd /c reg delete "HKEY_CURRENT_USER\\Software\\JavaSoft" /f')
