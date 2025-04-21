import os, requests, json
from functools import partial

opentext = partial(open, encoding='utf-8')

version_map_modrinth = {
    "1.19.2": ['1.19', '1.20'] + [f'1.19.{x}' for x in range(1, 5)] + [f'1.20.{x}' for x in range(1, 7)]
}
try:
    with open('versionByName.json', 'r') as f:
        cfRaw = json.load(f)
        version_map_curseforge = {}
        for src, dst in version_map_modrinth.items():
            version_map_curseforge[src] = sorted([cfRaw[d] for d in dst if d in cfRaw])
except Exception as e:
    import traceback
    traceback.print_exc(e)
    exit()

with opentext('secrets.json') as f:
    SECRETS = json.load(f)

with opentext('config.json') as f:
    CFG = json.load(f)

with opentext('changelog.md') as f:
    CHANGELOG = f.read()

if CFG.get('mock'):

    def mock_post(*a, **kw):
        print('POST', a, kw)
        return mock_post

    mock_post.text = 'MOCK'

    requests.post = mock_post


def push_file(file):
    filename = os.path.basename(file)
    print("UPLOADING:", filename)
    filename_body, filename_ext = os.path.splitext(filename)
    [mod_name, platform, game_version, mod_version] = filename_body.split('-')
    filename = f'{mod_name}-{platform}-{mod_version}' + filename_ext

    # https://docs.modrinth.com/api/operations/createversion/
    if "modrinth":
        header = {
            "Authorization": SECRETS['auth_mr'],
            "User-Agent": f"YukkuriC/{mod_name}",
            # "Content-Type": "multipart/form-data"
        }
        dep = [{
            "project_id": dep,
            "dependency_type": "required"
        } for dep in CFG['MR']['dependencies']] + [{
            "project_id": dep,
            "dependency_type": "optional"
        } for dep in CFG['MR']["optional"]]
        data = {
            "name": filename,
            "version_number": mod_version,
            "changelog": CHANGELOG,
            "dependencies": dep,
            "game_versions": version_map_modrinth.get(game_version, []),
            "version_type": "release",
            "loaders": [platform],
            "featured": True,
            "status": "listed",
            "project_id": CFG['MR']['project_id'],
            "file_parts": [filename],
            "primary_file": filename
        }

        response = requests.post("https://api.modrinth.com/v2/version",
                                 data={
                                     "data": json.dumps(data),
                                 },
                                 headers=header,
                                 files={filename: open(file, 'rb')})
        print("MR", response.text)

    # https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-upload-api
    if "curseforge":
        data = {
            "changelog": CHANGELOG,
            "changelogType": "markdown",
            "displayName": filename,
            "gameVersions": [
                # client & server
                *(9638, 9639),
                8326,  # java 17
                7498 if platform == 'forge' else 7499,  # forge or fabric
                *(version_map_curseforge.get(game_version, []))  # version range
            ],
            "releaseType": "release",
        }
        header = {
            "X-Api-Token": SECRETS['auth_cf'],
        }

        response = requests.post(f"https://minecraft.curseforge.com/api/projects/{CFG['CF']['project_id']}/upload-file",
                                 data={
                                     "metadata": json.dumps(data),
                                 },
                                 headers=header,
                                 files={"file": open(file, 'rb')})
        print("CF", response.text)


def pick_versions():
    header = {
        "X-Api-Token": SECRETS['auth_cf'],
    }
    versions = requests.get("https://minecraft.curseforge.com/api/game/versions", headers=header)
    with opentext('versions.json', 'w') as f:
        print(versions.text, file=f)
    data = json.loads(versions.text)
    versionByName = {}
    for entry in data:
        if entry["gameVersionTypeID"] == 75125:
            versionByName[entry['name']] = entry['id']
    with open('versionByName.json', 'w') as f:
        json.dump(versionByName, f)

    exit()


if __name__ == '__main__':
    # pick_versions()
    for sub in os.listdir('.'):
        if not sub.endswith('.jar'):
            continue
        push_file(sub)