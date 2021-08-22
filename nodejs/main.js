const path = require('path');
const {readdirSync, unlink, promises, writeFile} = require('fs')
const {parseString, Builder} = require('xml2js');
const {exec} = require('child_process');

const userProfilePath = path.join(process.env.APPDATA, 'JetBrains');
const appNames = ['IntelliJ', 'PyCharm', 'Rider', 'WebStorm', 'PhpStorm'];

const getAppDirectories = source =>
    readdirSync(source, {withFileTypes: true})
        .filter(dirent => dirent.isDirectory() && appNames.findIndex(n => dirent.name.includes(n)) > -1)
        .map(dirent => dirent.name)


getAppDirectories(userProfilePath).forEach(async (e) => {
    const appDirPath = path.join(userProfilePath, e);

    // Delete eval folder with licence key
    const evalDirPath = path.join(appDirPath, 'eval');
    const files = readdirSync(evalDirPath, {withFileTypes: true})
        .filter(dirent => dirent.isFile())
        .map(dirent => dirent.name)

    for (const file of files) {
        unlink(path.join(evalDirPath, file), err => {
            if (err) throw err;
        });
    }


    // Update options.xml
    const optionsFilePath = path.join(appDirPath, 'options', 'other.xml');

    try {
        const data = await promises.readFile(optionsFilePath)
        await parseString(data, async function (err, result) {
            if (!err) {
                const removedIndices = [];
                const properties = result['application']?.['component']?.find(n => n.hasOwnProperty('property'))?.['property'];
                properties.forEach((e, i) => {
                    if (e['$']?.name?.includes('evlsprt')) {
                        removedIndices.push(i);
                    }
                })

                removedIndices.forEach((e, i) => {
                    if (i > 0) {
                        e--;
                    }
                    properties.splice(e, 1);
                })

                const builder = new Builder();
                const writeData = builder.buildObject(result);
                await promises.writeFile(optionsFilePath, writeData);
            }
        });
    } catch (e) {
        console.log(e.message);
    }
})

// Delete registry key
exec('reg delete "HKEY_CURRENT_USER\\Software\\JavaSoft" /f', (err, stdout, stderr) => {
    if (err) {
        console.log(`stderr: ${stderr}`);
        return;
    }
    console.log(stdout);
});