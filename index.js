document.addEventListener('DOMContentLoaded', () => {
    
    // DOM Elements
    const card = document.getElementById('invitation-card');
    const pageLeft = document.getElementById('page-left');
    const btnToggleFold = document.getElementById('btn-toggle-fold');
    const sliderFold = document.getElementById('slider-fold');
    const labelFoldPercent = document.getElementById('label-fold-percent');
    const btnFlipCard = document.getElementById('btn-flip-card');
    
    const btnGuide = document.getElementById('btn-guide');
    const modalGuide = document.getElementById('modal-guide');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const btnGuideOk = document.getElementById('btn-guide-ok');
    
    const btnPrintMain = document.getElementById('btn-print-main');
    
    // Theme options and custom controls
    const themeCards = document.querySelectorAll('.theme-option-card');
    const selectFontNames = document.getElementById('select-font-names');
    
    // Input Fields
    const inputs = {
        'monogram': document.getElementById('input-monogram'),
        'names': document.getElementById('input-names'),
        'back-title-1': document.getElementById('input-back-title-1'),
        'back-title-conj': document.getElementById('input-back-title-conj'),
        'back-title-2': document.getElementById('input-back-title-2'),
        'back-subtitle': document.getElementById('input-back-subtitle'),
        
        'inside-left-title': document.getElementById('input-left-title'),
        'inside-left-subtitle': document.getElementById('input-left-subtitle'),
        'inside-left-p1': document.getElementById('input-left-p1'),
        'inside-left-p2': document.getElementById('input-left-p2'),
        
        'inside-right-header': document.getElementById('input-right-header'),
        'prog1-title': document.getElementById('input-prog1-title'),
        'prog1-loc': document.getElementById('input-prog1-loc'),
        'prog1-addr': document.getElementById('input-prog1-addr'),
        'prog2-title': document.getElementById('input-prog2-title'),
        'prog2-loc': document.getElementById('input-prog2-loc'),
        'prog2-addr': document.getElementById('input-prog2-addr'),
        'dress-label': document.getElementById('input-dress-label'),
        'closing-text': document.getElementById('input-closing-text'),
        'poetic-text': document.getElementById('input-poetic-text'),
        'poetic-gift': document.getElementById('input-poetic-gift')
    };

    // State Variables
    let isOpen = false;
    let isFlipped = false;
    let currentTheme = 'pearl';

    /* ==========================================================================
       DYNAMIC PEARLS GENERATION
       ========================================================================== */
    
    // Renders inside page top and bottom borders (curved pearl string)
    function renderPearlString(container, count, isBottom, isSecondary) {
        if (!container) return;
        
        const startX = isSecondary ? 7 : 3;
        const endX = isSecondary ? 93 : 97;
        const spanX = endX - startX;
        
        for (let i = 0; i < count; i++) {
            const pearl = document.createElement('div');
            pearl.className = 'pearl pearl-sway';
            
            // t ranges from -1 to 1 across the string
            const t = count > 1 ? (i - (count - 1)/2) / ((count - 1)/2) : 0;
            const xPercent = startX + (spanX / (count - 1)) * i;
            
            const maxDepth = isSecondary ? 24 : 38;
            const minDepth = isSecondary ? 5 : 10;
            const y = minDepth + maxDepth * (t * t);
            
            const minSize = isSecondary ? 5 : 8;
            const maxSize = isSecondary ? 9 : 14;
            const size = minSize + (maxSize - minSize) * (1 - Math.abs(t));
            
            pearl.style.width = `${size}px`;
            pearl.style.height = `${size}px`;
            pearl.style.left = `calc(${xPercent}% - ${size/2}px)`;
            
            if (isBottom) {
                pearl.style.bottom = `${y - size/2}px`;
            } else {
                pearl.style.top = `${y - size/2}px`;
            }
            
            pearl.style.animationDelay = `${i * 0.12}s`;
            container.appendChild(pearl);
        }
    }

    // Renders front cover scattering pearls (around borders and top, keeping center open)
    function renderFrontCoverPearls(container) {
        if (!container) return;
        container.innerHTML = '';
        const count = 42;
        for (let i = 0; i < count; i++) {
            const pearl = document.createElement('div');
            pearl.className = 'pearl pearl-sway';
            
            let x, y;
            const rand = Math.random();
            if (rand < 0.45) {
                // Top area (above heads)
                x = 5 + Math.random() * 90;
                y = 5 + Math.random() * 32;
            } else if (rand < 0.72) {
                // Left side cluster
                x = 5 + Math.random() * 22;
                y = 30 + Math.random() * 60;
            } else {
                // Right side cluster
                x = 73 + Math.random() * 22;
                y = 30 + Math.random() * 60;
            }
            
            const size = 5 + Math.random() * 9; // 5px to 14px
            
            pearl.style.width = `${size}px`;
            pearl.style.height = `${size}px`;
            pearl.style.left = `calc(${x}% - ${size/2}px)`;
            pearl.style.top = `calc(${y}% - ${size/2}px)`;
            pearl.style.animationDelay = `${Math.random() * 2.5}s`;
            
            container.appendChild(pearl);
        }
    }

    // Renders back cover bubbling pearls (bubbling up from bottom-left corner)
    function renderBackCoverPearls(container) {
        if (!container) return;
        container.innerHTML = '';
        const count = 45;
        for (let i = 0; i < count; i++) {
            const pearl = document.createElement('div');
            pearl.className = 'pearl pearl-sway';
            
            // Clustered heavily towards bottom-left
            // x from 0 to 75%
            const x = Math.pow(Math.random(), 1.4) * 75;
            // y from 40% to 96%
            const y = 38 + Math.pow(Math.random(), 1.1) * 58;
            
            const size = 6 + Math.random() * 11; // 6px to 17px
            
            pearl.style.width = `${size}px`;
            pearl.style.height = `${size}px`;
            pearl.style.left = `calc(${x}% - ${size/2}px)`;
            pearl.style.top = `calc(${y}% - ${size/2}px)`;
            pearl.style.animationDelay = `${Math.random() * 2.5}s`;
            
            container.appendChild(pearl);
        }
    }

    function initPearls() {
        const topContainers = document.querySelectorAll('.decorations-top.pearl-decor');
        const bottomContainers = document.querySelectorAll('.decorations-bottom.pearl-decor');
        
        topContainers.forEach(container => {
            container.innerHTML = '';
            renderPearlString(container, 20, false, false);
            renderPearlString(container, 17, false, true);
        });
        
        bottomContainers.forEach(container => {
            container.innerHTML = '';
            renderPearlString(container, 20, true, false);
            renderPearlString(container, 17, true, true);
        });

        // Front Cover pearls
        const frontContainers = document.querySelectorAll('.decorations-front.pearl-decor');
        frontContainers.forEach(container => {
            renderFrontCoverPearls(container);
        });

        // Back Cover pearls
        const backContainers = document.querySelectorAll('.decorations-back.pearl-decor');
        backContainers.forEach(container => {
            renderBackCoverPearls(container);
        });
    }

    /* ==========================================================================
       3D CARD INTERACTION
       ========================================================================== */
    
    // Toggle transitions on card pages (disable during range slider dragging)
    function enableTransitions(enable) {
        if (enable) {
            card.style.transition = 'transform var(--transition-slow) cubic-bezier(0.19, 1, 0.22, 1)';
            pageLeft.style.transition = 'transform var(--transition-slow) cubic-bezier(0.19, 1, 0.22, 1)';
        } else {
            card.style.transition = 'none';
            pageLeft.style.transition = 'none';
        }
    }

    function updateCardFold(value) {
        const percent = parseInt(value, 10);
        labelFoldPercent.textContent = `${percent}%`;
        
        // Left page swings from 0deg (closed) to -180deg (fully open)
        const angle = -1.8 * percent;
        pageLeft.style.transform = `rotateY(${angle}deg)`;
        
        // Card centers itself when opening (moves right by 50% card width)
        const tx = percent * 0.5;
        if (isFlipped) {
            card.style.transform = `rotateY(180deg) translateX(${-tx}%)`;
        } else {
            card.style.transform = `translateX(${tx}%)`;
        }

        // Adjust state variables based on position
        isOpen = percent > 50;
        
        // Update button text
        const openIcon = btnToggleFold.querySelector('.icon-open');
        const closeIcon = btnToggleFold.querySelector('.icon-close');
        if (isOpen) {
            openIcon.style.display = 'none';
            closeIcon.style.display = 'inline';
        } else {
            openIcon.style.display = 'inline';
            closeIcon.style.display = 'none';
        }
    }

    // Fold/Unfold button handler
    btnToggleFold.addEventListener('click', () => {
        enableTransitions(true);
        if (isOpen) {
            sliderFold.value = 0;
            updateCardFold(0);
        } else {
            sliderFold.value = 100;
            updateCardFold(100);
        }
    });

    // Slider inputs
    sliderFold.addEventListener('input', (e) => {
        enableTransitions(false);
        updateCardFold(e.target.value);
    });

    // Flip Card (front/back view)
    btnFlipCard.addEventListener('click', () => {
        enableTransitions(true);
        isFlipped = !isFlipped;
        
        const percent = parseInt(sliderFold.value, 10);
        const tx = percent * 0.5;
        
        if (isFlipped) {
            card.classList.add('is-flipped');
            card.style.transform = `rotateY(180deg) translateX(${-tx}%)`;
            btnFlipCard.textContent = isOpen ? "🔄 Voir l'intérieur" : "🔄 Voir le devant";
        } else {
            card.classList.remove('is-flipped');
            card.style.transform = `translateX(${tx}%)`;
            btnFlipCard.textContent = "🔄 Voir le dos";
        }
    });

    /* ==========================================================================
       THEME & DESIGN SYSTEM CONTROLS
       ========================================================================== */
    themeCards.forEach(themeCard => {
        themeCard.addEventListener('click', () => {
            themeCards.forEach(c => c.classList.remove('active'));
            themeCard.classList.add('active');
            
            const theme = themeCard.getAttribute('data-theme');
            setTheme(theme);
        });
    });

    function setTheme(theme) {
        const themeClasses = ['theme-pearl', 'theme-gold', 'theme-floral', 'theme-starry'];
        const newClass = `theme-${theme}`;
        
        const faces = [
            document.getElementById('face-front'),
            document.getElementById('face-inside-left'),
            document.getElementById('face-inside-right'),
            document.getElementById('face-back')
        ];
        
        const printPages = [
            document.getElementById('print-page-1'),
            document.getElementById('print-page-2')
        ];

        faces.forEach(face => {
            if (face) {
                themeClasses.forEach(tc => face.classList.remove(tc));
                face.classList.add(newClass);
            }
        });

        printPages.forEach(page => {
            if (page) {
                themeClasses.forEach(tc => page.classList.remove(tc));
                page.classList.add(newClass);
            }
        });

        currentTheme = theme;
    }

    // Font family change
    selectFontNames.addEventListener('change', (e) => {
        const font = e.target.value;
        const cursiveElements = document.querySelectorAll('.cursive-font');
        cursiveElements.forEach(el => {
            el.style.fontFamily = font;
        });
    });

    /* ==========================================================================
       REAL-TIME DATA BINDINGS
       ========================================================================== */
    function updateBoundElements(key, value) {
        const targets = document.querySelectorAll(`[data-bind="${key}"]`);
        targets.forEach(target => {
            // Check if text has line breaks
            if (key.includes('loc') || key.includes('addr') || key.includes('p1') || key.includes('p2') || key.includes('closing') || key.includes('poetic') || key.includes('dress')) {
                target.innerHTML = value.replace(/\n/g, '<br>');
            } else {
                target.textContent = value;
            }
        });
    }

    // Attach input listeners
    Object.keys(inputs).forEach(key => {
        const input = inputs[key];
        if (input) {
            input.addEventListener('input', (e) => {
                updateBoundElements(key, e.target.value);
            });
        }
    });

    // Special binding: names input should also update the monogram and signatures
    if (inputs['names']) {
        inputs['names'].addEventListener('input', (e) => {
            const namesText = e.target.value;
            const parts = namesText.split('&');
            let monoText = "J&E";
            if (parts.length >= 2) {
                const p1 = parts[0].trim().charAt(0);
                const p2 = parts[1].trim().charAt(0);
                if (p1 && p2) {
                    monoText = `${p1}&${p2}`;
                }
            } else if (namesText.trim().length > 0) {
                monoText = namesText.trim().charAt(0);
            }
            
            const monoInput = inputs['monogram'];
            if (monoInput) {
                monoInput.value = monoText;
            }
            updateBoundElements('monogram', monoText);
        });
    }

    /* ==========================================================================
       PHOTO UPLOADER AND PRELOADING
       ========================================================================== */
    const inputPhoto = document.getElementById('input-photo');
    const coverFace = document.getElementById('face-front');
    const printCoverFace = document.getElementById('print-face-front');
    const insideLeft = document.getElementById('face-inside-left');
    const printInsideLeft = document.getElementById('print-face-inside-left');
    const insideRight = document.getElementById('face-inside-right');
    const printInsideRight = document.getElementById('print-face-inside-right');

    function applyCouplePhoto(url) {
        if (!url) return;
        
        // Set cover backgrounds
        if (coverFace) coverFace.style.backgroundImage = `url(${url})`;
        if (printCoverFace) printCoverFace.style.backgroundImage = `url(${url})`;
        
        // Set inside watermarks using CSS variable
        const watermarkVal = `url(${url})`;
        if (insideLeft) insideLeft.style.setProperty('--watermark-url', watermarkVal);
        if (printInsideLeft) printInsideLeft.style.setProperty('--watermark-url', watermarkVal);
        if (insideRight) insideRight.style.setProperty('--watermark-url', watermarkVal);
        if (printInsideRight) printInsideRight.style.setProperty('--watermark-url', watermarkVal);
    }

    if (inputPhoto) {
        inputPhoto.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const url = URL.createObjectURL(file);
                applyCouplePhoto(url);
            }
        });
    }

    // Preload default couple image (local couple photo)
    applyCouplePhoto('couple.jpg');

    /* ==========================================================================
       TAB BAR CONTROLS
       ========================================================================== */
    const tabBtns = document.querySelectorAll('.tab-btn');
    const panels = document.querySelectorAll('.editor-panel');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabId = btn.getAttribute('data-tab');
            
            tabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            panels.forEach(p => p.classList.remove('active'));
            const activePanel = document.getElementById(tabId);
            if (activePanel) activePanel.classList.add('active');
        });
    });

    /* ==========================================================================
       MODAL DIALOG
       ========================================================================== */
    function openModal() {
        modalGuide.style.display = 'flex';
    }

    function closeModal() {
        modalGuide.style.display = 'none';
    }

    btnGuide.addEventListener('click', openModal);
    btnCloseModal.addEventListener('click', closeModal);
    btnGuideOk.addEventListener('click', closeModal);
    
    modalGuide.addEventListener('click', (e) => {
        if (e.target === modalGuide) {
            closeModal();
        }
    });

    /* ==========================================================================
       PRINTING TRIGGER
       ========================================================================== */
    btnPrintMain.addEventListener('click', () => {
        const confirmPrint = confirm(
            "Prêt pour l'impression / PDF ?\n\n" +
            "Pour un résultat optimal :\n" +
            "1. Choisissez le format 'Paysage' (Landscape)\n" +
            "2. Activez les 'Graphiques d'arrière-plan' (Background graphics)\n" +
            "3. Réglez les marges sur 'Aucune' (None)\n" +
            "4. En recto-verso, retournez sur les BORDS COURTS."
        );
        
        if (confirmPrint) {
            window.print();
        }
    });

    /* ==========================================================================
       INITIALIZATION
       ========================================================================== */
    initPearls();
    setTheme(currentTheme);
    updateCardFold(0);
});
