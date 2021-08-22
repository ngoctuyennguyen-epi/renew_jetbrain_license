using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml;

namespace CSharp
{
    class Program
    {
        static void Main(string[] args)
        {
            var UserProfilePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "JetBrains");
            var AppNames = new List<string> { "IntelliJ", "PyCharm", "Rider", "WebStorm", "PhpStorm" };

            var UserProfileDirectory = new DirectoryInfo(UserProfilePath);

            var AppDirectories = UserProfileDirectory.GetDirectories().Where(n => AppNames.Any(e => n.Name.Contains(e, StringComparison.OrdinalIgnoreCase))).ToArray();

            if (AppDirectories.Count() > 0)
            {
                foreach (var AppDirectory in AppDirectories)
                {
                    // Delete eval folder with licence key
                    var EvalDirPath = Path.Combine(AppDirectory.FullName, "eval");
                    var EvalFiles = Directory.GetFiles(EvalDirPath);
                    Array.ForEach(EvalFiles, File.Delete);

                    // Update options.xml
                    var OptionsFilePath = Path.Combine(AppDirectory.FullName, "options", "other.xml");
                    if (!File.Exists(OptionsFilePath))
                    {
                        Console.WriteLine(string.Format("%s not found", OptionsFilePath));
                    }

                    var document = new XmlDocument();
                    document.Load(OptionsFilePath);

                    var root = document.DocumentElement;
                    var nodeList = root.SelectNodes("//property[contains(@name,'evlsprt')]");

                    for (int i = nodeList.Count - 1; i >= 0; i--)
                    {
                        nodeList[i].ParentNode.RemoveChild(nodeList[i]);
                    }

                    document.Save(OptionsFilePath);
                }
            }

            // Delete registry key
            System.Diagnostics.Process.Start("cmd.exe", @" /k reg delete HKEY_CURRENT_USER\Software\JavaSoft /f ");
        }
    }
}
