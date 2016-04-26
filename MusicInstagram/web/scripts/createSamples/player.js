
function Player() {
    window.AudioContext = window.AudioContext || window.webkitAudioContext;
    this.context = new AudioContext();
    this.startOffset = 0;
    this.isPlaying = false;
    this.buffer = null;
    this.sourceNode = null;
    this.analyserNode = this.context.createAnalyser();
    this.gainNode = this.context.createGain();
    this.sourceConnection = this.analyserNode;
    this.analyserNode.connect(this.gainNode);
    this.gainNode.connect(this.context.destination);
}

Player.prototype.getContext = function () {
    return this.context;
};

Player.prototype.toggle = function () {
    this.isPlaying ? this.pause() : this.play();
};

Player.prototype.play = function () {
    if (!this.isPlaying && this.buffer) {
        this.isPlaying = true;
        this.sourceNode = this.context.createBufferSource();
        this.sourceNode.buffer = this.buffer;
        this.sourceNode.loop = false;
        this.sourceNode.connect(this.sourceConnection);

        //NOT WORKING
//        var self = this;
//        this.sourceNode.onended = function () {
//            self.isPlaying = false;
//            self.startOffset = 0;
//            console.log("onended");
//        };

        this.sourceNode.start(0, this.startOffset);
        this.startTime = this.context.currentTime;
    }
};

Player.prototype.pause = function () {
    if (this.isPlaying) {
        this.isPlaying = false;
        this.startOffset += this.context.currentTime - this.startTime;
        this.startOffset %= this.buffer.duration;
        this.sourceNode.stop();
        this.sourceNode.disconnect();
    }
};

Player.prototype.stop = function () {
    this.pause();
    this.startOffset = 0;
};

Player.prototype.loadSound = function (url, callback) {
    this.stop();
    this.buffer = null;
    var request = new XMLHttpRequest();
    request.open('GET', url, true);
    request.responseType = 'arraybuffer';
    var self = this;
    // Decode asynchronously
    request.onload = function () {
        self.context.decodeAudioData(request.response, function (buf) {
            self.buffer = buf;
            if (callback)
                callback();
        }, function () {
            console.log('Download error');
        });
    };
    request.send();
};

Player.prototype.getPosition = function () {
    var timeElapsed = this.startOffset;
    if (this.isPlaying) {
        timeElapsed += this.context.currentTime - this.startTime;
    }
    if (timeElapsed > this.buffer.duration) {
        this.stop();
        timeElapsed = 0;
    }
    // timeElapsed %= this.buffer.duration;
    var fraction = timeElapsed / this.buffer.duration;
    return fraction;
};

Player.prototype.setPosition = function (fraction) {
    if (!this.isPlaying) {
        this.startOffset = this.buffer.duration * fraction;
    } else {
        this.pause();
        this.startOffset = this.buffer.duration * fraction;
        this.play();
    }
};

Player.prototype.getVolume = function () {
    return this.gainNode.gain.value;
};

Player.prototype.setVolume = function (fraction) {
    this.gainNode.gain.value = fraction;
};

Player.prototype.setSourceConnection = function (node) {
    this.sourceConnection = node;
};

Player.prototype.getLastNode = function () {
    return this.analyserNode;
};

//$(function () {
//    var player = new Player();
//    var visualizer = new Visualizer(player.analyserNode, document.querySelector("#frequencyCanvas"));
//    var recorder = new Recorder(player.analyserNode);
//    var effectsManager = new EffectsManager(player.getContext(), player.getLastNode());
//    player.setSourceConnection(effectsManager.getInputNode());
//    var isRecording = false;
//
//    $("#instantUpload").on("click", function (event) {
//        var context = new OfflineAudioContext(2, 44100 * 40, 44100); // check is args neccessary
//        var buffer = player.buffer; // TODO make common buffer for player and instantUpload
//        var sr = $("#slider-range");
//        var start = sr.slider("values", 0) / sr.slider("option", "max");
//        var end = sr.slider("values", 1) / sr.slider("option", "max");
//        var offset = start * buffer.duration;
//        var duration = (end - start) * buffer.duration;
//        var offlineEffectsManager = new EffectsManager(context, context.destination);
//        var inputNode = offlineEffectsManager.getInputNode();
//        var url = player.context; // TODO set url
//        // init offlineEffectsManager from effectsManager
//        for (var e = effectsManager.firstNode.next; e !== effectsManager.lastNode; e = e.next) {
//            offlineEffectsManager.addEffect(e.name);
//        }
//        instantUpload(context, buffer, offset, duration, inputNode, url);
//    });
//    $("#uploadButton").on("click", function (event) {
//        uploadFile();
//    });
//    $("#downloadButton").on("click", function (event) {
//        $("#toggleButton").attr('disabled', true);
//        var fileName = $('#inputFile').get(0).files[0].name;
//        player.loadSound("./upload/" + fileName, function () {
//            $("#toggleButton").attr('disabled', false);
//            drawTimeLine(player.buffer, $("#slider-range"));
//        });
//    });
//    $("#toggleButton").on("click", function (event) {
//        player.toggle();
//    });
//    $("#stopButton").on("click", function (event) {
//        player.stop();
//        $("#positionRange").get(0).value = 0;
//    });
//    $("#addEffectButton").on("click", function (event) {
//        var effect = $("#effectsSelect :selected").text();
//        var id = effectsManager.addEffect(effect);
//        deleteButton = $('<input type="button" value="Delete ' + effect + '" id="deleteEffect' + id + '">');
//        deleteButton.appendTo('#deleteEffect');
//        $("#deleteEffect" + id).on("click", function () {
//            effectsManager.deleteEffect(id);
//        });
//    });
//    $("#volumeRange").on("input", function () {
//        var element = $("#volumeRange").get(0);
//        var fraction = parseInt(element.value) / parseInt(element.max);
//        player.setVolume(fraction * fraction);
//    });
//    $("#positionRange").on("input", function () {
//        element = $("#positionRange").get(0);
//        var fraction = parseInt(element.value) / parseInt(element.max);
//        player.setPosition(fraction);
//    });
//    $("#recButton").on("click", function () {
//        isRecording ? recorder.record() : recorder.stop();
//        isRecording = !isRecording;
//    });
//    $("#clearRecButton").on("click", function () {
//        if (isRecording)
//            recorder.stop();
//        recorder.clear();
//    });
//    $("#uploadRecButton").on("click", function () {
//        if (isRecording)
//            recorder.stop();
//        recorder.exportWAV(uploadWAV);
//    });
//    setInterval(function () {
//        if (player.isPlaying) {
//            var element = $("#positionRange").get(0);
//            element.value = element.max * player.getPosition();
//        }
//    }, 1000);
//});
function uploadFile() {
    var documentData = new FormData();
    documentData.append('uploadedFile', $('#inputFile')[0].files[0]);
    var fileName = $('#inputFile').get(0).files[0].name;
    documentData.append('fileName', fileName);
    $.ajax({
        url: "./webresources/generic/upload",
        type: 'POST',
        data: documentData,
        cache: false,
        contentType: false,
        processData: false,
        success: function (responseText) {
            $('#progress_percent').text(responseText);
        },
        xhr: function () {
            var myXhr = $.ajaxSettings.xhr();
            if (myXhr.upload) {
                myXhr.upload.addEventListener('progress', progress, false);
            } else {
                console.log('Upload progress is not supported.');
            }
            return myXhr;
        }
    });
}
function progress(e) {
    if (e.lengthComputable) {
        $('#progress_percent').text(Math.floor((e.loaded * 100) / e.total) + "%");
        $('progress').attr({value: e.loaded, max: e.total});
    }
}

function EffectNode() {
    this.id = EffectNode.lastId++;
    this.name = null;
    this.value = null;
    this.next = null;
    this.prev = null;
}
EffectNode.lastId = 0;

function EffectsManager(/*context, outputNode*/) {
    //this.context = context;
    //this.tuna = new Tuna(this.context);
    //this.outputNode = outputNode;

    this.firstNode = new EffectNode();
    this.lastNode = new EffectNode();
    this.firstNode.next = this.lastNode;
    this.lastNode.prev = this.firstNode;

    //this.firstNode.value = this.context.createGain();
    //this.lastNode.value = this.context.createGain();

    //this.lastNode.value.connect(this.outputNode);
    //this.firstNode.value.connect(this.lastNode.value);
}

EffectsManager.prototype.updateContext = function () {
    this.tuna = new Tuna(this.context);
};

EffectsManager.prototype.updateNodes = function () {
    this.updateContext();
    for (var current = this.firstNode; current !== null; current = current.next) {
        if (current.value !== null) {
            current.value.disconnect();
        }
    }
    this.firstNode.value = this.context.createGain();
    this.lastNode.value = this.context.createGain();
    for (var current = this.firstNode.next; current !== this.lastNode; current = current.next) {
        current.value = this.getEffectByName(current.name);
    }
    for (var current = this.firstNode.next; current !== this.lastNode; current = current.next) {
        current.value.connect(current.next.value);
    }
    this.firstNode.value.connect(this.firstNode.next.value);
    this.lastNode.value.connect(this.outputNode);
};

EffectsManager.prototype.getInputNode = function () {
    this.updateContext();
    return this.firstNode.value;
};

EffectsManager.prototype.getEffectByName = function (effect) {
    switch (effect) {
        case 'chorus':
            return new this.tuna.Chorus({
                rate: 1.5, //0.01 to 8+
                feedback: 0.2, //0 to 1+
                delay: 0.0045, //0 to 1
                bypass: 0 //the value 1 starts the effect as bypassed, 0 or 1
            });
            break
        case 'delay':
            return new this.tuna.Delay({
                feedback: 0.45, //0 to 1+
                delayTime: 150, //how many milliseconds should the wet signal be delayed?
                wetLevel: 0.25, //0 to 1+
                dryLevel: 1, //0 to 1+
                cutoff: 2000, //cutoff frequency of the built in lowpass-filter. 20 to 22050
                bypass: 0
            });
            break
        case 'phaser':
            return new this.tuna.Phaser({
                rate: 1.2, //0.01 to 8 is a decent range, but higher values are possible
                depth: 0.3, //0 to 1
                feedback: 0.2, //0 to 1+
                stereoPhase: 30, //0 to 180
                baseModulationFrequency: 700, //500 to 1500
                bypass: 0
            });
            break
        case 'overdrive':
            return new this.tuna.Overdrive({
                outputGain: 0.5, //0 to 1+
                drive: 0.7, //0 to 1
                curveAmount: 1, //0 to 1
                algorithmIndex: 0, //0 to 5, selects one of our drive algorithms
                bypass: 0
            });
            break
        case 'compressor':
            return new this.tuna.Compressor({
                threshold: 0.5, //-100 to 0
                makeupGain: 1, //0 and up
                attack: 1, //0 to 1000
                release: 0, //0 to 3000
                ratio: 4, //1 to 20
                knee: 5, //0 to 40
                automakeup: true, //true/false
                bypass: 0
            });
            break
        case 'convolver':
            return new this.tuna.Convolver({
                highCut: 22050, //20 to 22050
                lowCut: 20, //20 to 22050
                dryLevel: 1, //0 to 1+
                wetLevel: 1, //0 to 1+
                level: 1, //0 to 1+, adjusts total output of both wet and dry
                impulse: "impulses/filter-telephone.wav", //the path to your impulse response
                bypass: 0
            });
            break
        case 'filter':
            return new this.tuna.Filter({
                frequency: 440, //20 to 22050
                Q: 1, //0.001 to 100
                gain: 0, //-40 to 40
                filterType: "lowpass", //lowpass, highpass, bandpass, lowshelf, highshelf, peaking, notch, allpass
                bypass: 0
            });
            break
        case 'cabinet':
            return new this.tuna.Cabinet({
                makeupGain: 1, //0 to 20
                impulsePath: "impulses/smooth-hall.wav", //path to your speaker impulse
                bypass: 0
            });
            break
        case 'tremolo':
            return new this.tuna.Tremolo({
                intensity: 0.3, //0 to 1
                rate: 4, //0.001 to 8
                stereoPhase: 0, //0 to 180
                bypass: 0
            });
            break
        case 'wahwah':
            return new this.tuna.WahWah({
                automode: true, //true/false
                baseFrequency: 0.5, //0 to 1
                excursionOctaves: 2, //1 to 6
                sweep: 0.2, //0 to 1
                resonance: 10, //1 to 100
                sensitivity: 0.5, //-1 to 1
                bypass: 0
            });
            break
        case 'bitcrusher':
            return new this.tuna.Bitcrusher({
                bits: 4, //1 to 16
                normfreq: 0.1, //0 to 1
                bufferSize: 4096  //256 to 16384
            });
            break
        case 'moog':
            return new this.tuna.MoogFilter({
                cutoff: 0.065, //0 to 1
                resonance: 3.5, //0 to 4
                bufferSize: 4096  //256 to 16384
            });
            break
        case 'glitch':
            return new this.tuna.PingPongDelay({
                wetLevel: 0.5, //0 to 1
                feedback: 0.3, //0 to 1
                delayTimeLeft: 150, //1 to 10000 (milliseconds)
                delayTimeRight: 200 //1 to 10000 (milliseconds)
            });
            break
    }
};

EffectsManager.prototype.addDummyEffect = function (effect) {
    var e = new EffectNode();
    e.name = effect;

    var prev = this.lastNode.prev;
    var next = this.lastNode;
    prev.next = e;
    next.prev = e;
    e.next = next;
    e.prev = prev;

    return e;
};

EffectsManager.prototype.addEffect = function (effect) {
//    this.updateContext();
//    var e = new EffectNode();
//    e.name = effect;
//    e.value = this.getEffectByName(effect);
//
//    var prev = this.lastNode.prev;
//    var next = this.lastNode;
//    prev.next = e;
//    next.prev = e;
//    e.next = next;
//    e.prev = prev;
//
//    e.value.connect(next.value);
//    prev.value.disconnect();
//    prev.value.connect(e.value);
//
//    return e.id;
    this.updateContext();
    var e = this.addDummyEffect(effect);
    e.value = this.getEffectByName(effect);
    
    e.value.connect(e.next.value);
    e.prev.value.disconnect();
    e.prev.value.connect(e.value);

    return e.id;
};

EffectsManager.prototype.deleteEffect = function (id) {
    this.updateContext();
    var current = this.firstNode.next;
    while (current !== this.lastNode) {
        if (current.id === id) {
            var prev = current.prev;
            var next = current.next;
            prev.next = next;
            next.prev = prev;

            if (prev.value !== null) {
                prev.value.disconnect();
                if (next.vaule !== null) {
                    prev.value.connect(next.value);
                }
            }
            if (current.value !== null) {
                current.value.disconnect();
            }
            current = null;
            $('#deleteEffect' + id).remove();
            break;
        }
        current = current.next;
    }
};

function uploadWAV(blob) {
    var documentData = new FormData();
    documentData.append('uploadedFile', blob);
    fileName = "record.wav";
    documentData.append('fileName', fileName);
    $.ajax({
        url: "./webresources/generic/upload",
        type: 'POST',
        data: documentData,
        cache: false,
        contentType: false,
        processData: false,
        success: function (responseText) {
            $('#progress_percent').text(responseText);
        },
        xhr: function () {
            var myXhr = $.ajaxSettings.xhr();
            if (myXhr.upload) {
                myXhr.upload.addEventListener('progress', progress, false);
            } else {
                console.log('Upload progress is not supported.');
            }
            return myXhr;
        }
    });
}

function drawTimeLine(buffer, element) {
    var canvas = document.createElement("canvas");
    var drawContext = canvas.getContext('2d');
    canvas.width = 600;
    canvas.height = 100;
    var pointsWidth = 20000;
    var data = buffer.getChannelData(0);
    var step = Math.floor(data.length / pointsWidth);
    var width = canvas.width / pointsWidth;
    // make lines wider
    drawContext.fillStyle = 'blue';
    for (var i = 0; i < pointsWidth; ++i) {
        var fraction = Math.abs(data[i * step]);
        var height = canvas.height * fraction;
        var x = i / pointsWidth * canvas.width;
        var y = (canvas.height - height) / 2;
        //var hue = fraction * 360;
        //drawContext.fillStyle = 'hsl(' + hue + ', 100%, 50%)';
        drawContext.fillRect(x, y, width, height);
    }
    // configure slider
    element.css({'height': canvas.height, 'width': canvas.width, 'background-image': 'url(' + canvas.toDataURL("image/png") + ')'});
    element.slider({
        range: true,
        min: 0,
        max: 1000,
        values: [0, 1000],
        slide: function (event, ui) {
            //$("#amount").val("$" + ui.values[ 0 ] + " - $" + ui.values[ 1 ]);
        }
    });
    //$("#amount").val("$" + $("#slider-range").slider("values", 0) +
    //" - $" + $("#slider-range").slider("values", 1));
}

function Visualizer(analyserNode, canvas) {
    this.analyser = analyserNode;
    this.canvas = canvas;
    this.analyser.minDecibels = -140;
    this.analyser.maxDecibels = 0;

    this.freqs = new Uint8Array(this.analyser.frequencyBinCount);
    this.times = new Uint8Array(this.analyser.frequencyBinCount);

    window.requestAnimationFrame =
            window.requestAnimationFrame ||
            window.mozRequestAnimationFrame ||
            window.webkitRequestAnimationFrame ||
            window.msRequestAnimationFrame;
    requestAnimationFrame(this.draw.bind(this));
}

Visualizer.prototype.draw = function () {
    this.analyser.smoothingTimeConstant = 0.8;
    this.analyser.fftSize = 2048;

    // Get the frequency data from the currently playing music
    this.analyser.getByteFrequencyData(this.freqs);
    this.analyser.getByteTimeDomainData(this.times);

    var drawContext = this.canvas.getContext('2d');
    this.canvas.width = 640;
    this.canvas.height = 360;
    // Draw the frequency domain chart.
    for (var i = 0; i < this.analyser.frequencyBinCount; i++) {
        var value = this.freqs[i];
        var percent = value / 256;
        var height = this.canvas.height * percent;
        var offset = this.canvas.height - height - 1;
        var barWidth = this.canvas.width / this.analyser.frequencyBinCount;
        var hue = i / this.analyser.frequencyBinCount * 360;
        drawContext.fillStyle = 'hsl(' + hue + ', 100%, 50%)';
        drawContext.fillRect(i * barWidth, offset, barWidth, height);
    }

    // Draw the time domain chart.
//    for (var i = 0; i < this.analyser.frequencyBinCount; i++) {
//        var value = this.times[i];
//        var percent = value / 256;
//        var height = this.canvas.height * percent;
//        var offset = this.canvas.height - height - 1;
//        var barWidth = this.canvas.width / this.analyser.frequencyBinCount;
//        drawContext.fillStyle = 'black';
//        drawContext.fillRect(i * barWidth, offset, 1, 2);
//    }

    window.requestAnimationFrame =
            window.requestAnimationFrame ||
            window.mozRequestAnimationFrame ||
            window.webkitRequestAnimationFrame ||
            window.msRequestAnimationFrame;
    requestAnimationFrame(this.draw.bind(this));
};

/*
 * context = Web Audio API offline context
 * buffer = buffer where sound loaded
 * offset = time in seconds relative to buffer
 * duration = time in seconds relative to buffer
 * inputNode = first effect
 * url = where to send
 */
function instantGenerate(context, buffer, offset, duration, inputNode, url) {
    var sourceNode = context.createBufferSource();
    sourceNode.buffer = buffer;
    sourceNode.loop = false;
    sourceNode.connect(inputNode);
    sourceNode.start(0, offset, duration);
    context.startRendering().then(function (renderedBuffer) {
        console.log('Rendering completed successfully');
        window.AudioContext = window.AudioContext || window.webkitAudioContext;
        var audioCtx = new AudioContext();
        var song = audioCtx.createBufferSource();
        song.buffer = renderedBuffer;
        song.connect(audioCtx.destination);
        song.start();
    }).catch(function (err) {
        console.log('Rendering failed: ' + err);
        // Note: The promise should reject when startRendering is called a second time on an OfflineAudioContext
    });
}