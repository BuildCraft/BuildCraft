"use strict";

var fs = require("fs");
var Git = require("nodegit");
var request = require("request");

function req(url, callback) {
    request({
        url: `https://api.github.com/${url.replace("TOKEN", "access_token=" + fs.readFileSync("local/token.txt"))}`,
        headers: {
            "User-Agent": "bc-license-checker"
        }
    }, (error, response, body) => {
        callback(JSON.parse(body), response.headers)
    });
}

var commands = {
    sha_nick_remove: () => {
        fs.unlink("local/sha_nick.txt", err => {
            if(err) {
                console.log(err);
            }
        });
    },
    sha_nick_create: () => {
        function fetchCommitsPage(sha, page) {
            req(`repos/BuildCraft/BuildCraft/commits?TOKEN&page=${page}&sha=${sha}`, (response, headers) => {
                var link = headers.link, hasNext = true;

                if(link.indexOf("last") != -1) {
                    var last = link.substr(0, link.indexOf('>; rel="last"') - 1);
                    last = last.substr(last.lastIndexOf("page="));
                    last = last.substr("page=".length, last.indexOf("&") - "page=".length);
                } else {
                    last = page;
                    hasNext = false;
                }

                console.log(`Fetched page: ${page} of ${last}, ${response.length} commits`);

                var text = "";
                response.forEach(commit => {
                    if(commit.author) {
                        text += `${commit.sha}:${commit.author.login}\n`;
                    }
                });
                fs.appendFile("local/sha_nick.txt", text, err => {
                    if(err) {
                        console.log(err)
                    }
                    if(hasNext) {
                        fetchCommitsPage(sha, page + 1);
                    }
                });
            });
        }

        req("repos/BuildCraft/BuildCraft/git/refs/heads/8.0.x?TOKEN", response => {
            console.log(`Latest commit is: ${response.object.sha}`);
            fetchCommitsPage(response.object.sha, 1);
        });
    },
    file_nicks_remove: () => {
        fs.unlink("local/file_nicks.json", err => {
            if(err) {
                console.log(err);
            }
        });
    },
    file_nicks_create: () => {
        fs.readFile("local/sha_nick.txt", "utf-8", (err, data) => {
            if(err) {
                console.log(err);
            }

            var shaNick = {};
            data.split("\n").forEach((line) => {
                if(line.indexOf(":") != -1) {
                    var parts = line.split(":");
                    shaNick[parts[0]] = parts[1];
                }
            });

            console.log(`Read ${Object.keys(shaNick).length} sha-nick pairs`);

            var fileNicks = {};

            Git.Repository
                .open("/home/user/Projects/BuildCraft")
                .then(repo => repo.getBranchCommit("8.0.x"))
                .then(firstCommit => {
                    var history = firstCommit.history();
                    var count = 0;
                    history.on("commit", commit => {
                        var sha = commit.sha();
                        if(shaNick[sha]) {
                            count++
                        }
                    });
                    history.on("end", () => {
                        var history = firstCommit.history();
                        var i = 0;
                        history.on("commit", commit => {
                            var sha = commit.sha();
                            var nick = shaNick[sha];
                            if(shaNick[sha]) {
                                commit
                                    .getDiff()
                                    .then(diffs => {
                                        Promise.all(
                                            diffs.map(diff => diff.patches())
                                        ).then(patcheses => {
                                            i++;
                                            console.log(`Patcheses ready for ${i} of ${count}`);

                                            patcheses.forEach((patches) => {
                                                patches.forEach((patch) => {
                                                    var path = patch.newFile().path();

                                                    if(fileNicks[path]) {
                                                        if(fileNicks[path].indexOf(nick) == -1) {
                                                            fileNicks[path].push(nick);
                                                        }
                                                    } else {
                                                        fileNicks[path] = [nick];
                                                    }
                                                });
                                            });

                                            if(i == count) {
                                                fs.writeFile("local/file_nicks.json", JSON.stringify(fileNicks), err => {
                                                    if(err) {
                                                        console.log(err);
                                                    }

                                                    console.log(`Wrote ${Object.keys(fileNicks).length} file-nicks pairs`);
                                                });
                                            }
                                        });
                                    });
                            }
                        });
                        history.start();
                    });
                    history.start();
                });
        });
    },
    file_percent_remove: () => {
        fs.unlink("local/file_percent.json", err => {
            if(err) {
                console.log(err);
            }
        });
    },
    file_percent_create: () => {
        fs.readFile("local/file_nicks.json", "utf-8", (err, data) => {
            if(err) {
                console.log(err);
            }

            var fileNicks = JSON.parse(data);
            console.log(`Read ${Object.keys(fileNicks).length} file-nicks pairs`);

            fs.readFile("nicks_given.txt", "utf-8", (err, data) => {
                if(err) {
                    console.log(err);
                }

                var nicksGiven = data.split("\n");
                console.log(`Read ${nicksGiven.length} nicks`);
                
                var filePercent = {};
                
                for(var file in fileNicks) {
                    if(fileNicks.hasOwnProperty(file)) {
                        var nicks = fileNicks[file];
                        var countGiven = nicks.filter(nick => nicksGiven.indexOf(nick) != -1).length;
                        filePercent[file] = countGiven / nicks.length;
                    }
                }

                fs.writeFile("local/file_percent.json", JSON.stringify(filePercent), err => {
                    if(err) {
                        console.log(err);
                    }

                    console.log(`Wrote ${Object.keys(fileNicks).length} file-percent pairs`);
                });
            });
        });
    },
    html_result_remove: () => {
        fs.unlink("local/result.html", err => {
            if(err) {
                console.log(err);
            }
        });
    },
    html_result_create: () => {
        fs.readFile("local/file_percent.json", "utf-8", (err, data) => {
            if(err) {
                console.log(err);
            }

            var filePercent = JSON.parse(data);
            console.log(`Read ${Object.keys(filePercent).length} file-percent pairs`);

            fs.readFile("local/file_nicks.json", "utf-8", (err, data) => {
                if(err) {
                    console.log(err);
                }

                var fileNicks = JSON.parse(data);
                console.log(`Read ${Object.keys(fileNicks).length} file-nicks pairs`);

                var htmlResult = "";

                for(var file in filePercent) {
                    if(filePercent.hasOwnProperty(file) && fileNicks.hasOwnProperty(file)) {
                        var percent = filePercent[file];
                        var nicks = fileNicks[file];

                        var color = `rgb(${((1 - percent) * 255).toFixed(0)}, ${(percent * 255).toFixed(0)}, 0)`;
                        htmlResult += `<div style="background: ${color}" title="${(percent * 100).toFixed(2)}%: ${nicks.join(", ")}">${file}</div>\n`;
                    }
                }

                fs.writeFile("local/html_result.html", htmlResult, err => {
                    if(err) {
                        console.log(err);
                    }

                    console.log("Wrote html result");
                });
            });
        });
    }
};

if(process.argv[2] && commands[process.argv[2]]) {
    commands[process.argv[2]]();
} else {
    console.log(`Usage: node app ${Object.keys(commands).join("|")}`);
}