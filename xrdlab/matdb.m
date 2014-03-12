function matZ = matdb()
    matZ.Si_004.xyspace = 5.4309e-10;
    matZ.Si_004.poissonratio = 0.2800;
    matZ.Si_004.zspace = matZ.Si_004.xyspace/4;
    matZ.Si_004.V = matZ.Si_004.xyspace^3;
    matZ.Si_004.Z = zincblende_Z(14,14);
    matZ.Si_004.occupation = zincblende_occupation(1);
    matZ.Si_004.H = [4,0,0];
    matZ.Si_004.r = zincblende_r();

    matZ.Ge_004.xyspace = 5.6578e-10;
    matZ.Ge_004.poissonratio = 0.2730;
    matZ.Ge_004.zspace = matZ.Ge_004.xyspace/4;
    matZ.Ge_004.V = matZ.Ge_004.xyspace^3;
    matZ.Ge_004.Z = zincblende_Z(32,32);
    matZ.Ge_004.occupation = zincblende_occupation(1);
    matZ.Ge_004.H = [4,0,0];
    matZ.Ge_004.r = zincblende_r();

    matZ.GaAs_002.poissonratio = 0.3117;
    matZ.GaAs_002.xyspace = 5.6532e-10;
    matZ.GaAs_002.zspace = matZ.GaAs_002.xyspace/2;
    matZ.GaAs_002.V = matZ.GaAs_002.xyspace^3;
    matZ.GaAs_002.Z = zincblende_Z(31,33);
    matZ.GaAs_002.occupation = zincblende_occupation(1);
    matZ.GaAs_002.H = [2,0,0];
    matZ.GaAs_002.r = zincblende_r();

    matZ.GaAs_004.xyspace = 5.6532e-10;
    matZ.GaAs_004.poissonratio = 0.3117;
    matZ.GaAs_004.zspace = matZ.GaAs_004.xyspace/4;
    matZ.GaAs_004.V = matZ.GaAs_004.xyspace^3;
    matZ.GaAs_004.Z = zincblende_Z(31,33);
    matZ.GaAs_004.occupation = zincblende_occupation(1);
    matZ.GaAs_004.H = [4,0,0];
    matZ.GaAs_004.r = zincblende_r();

    matZ.GaP_002.xyspace = 5.4505e-10;
    matZ.GaP_002.poissonratio = 0.3070;
    matZ.GaP_002.zspace = matZ.GaP_002.xyspace/2;
    matZ.GaP_002.V = matZ.GaP_002.xyspace^3;
    matZ.GaP_002.Z = zincblende_Z(31,15);
    matZ.GaP_002.occupation = zincblende_occupation(1);
    matZ.GaP_002.H = [2,0,0];
    matZ.GaP_002.r = zincblende_r();

    matZ.GaP_004.xyspace = 5.4505e-10;
    matZ.GaP_004.poissonratio = 0.3070;
    matZ.GaP_004.zspace = matZ.GaP_004.xyspace/4;
    matZ.GaP_004.V = matZ.GaP_004.xyspace^3;
    matZ.GaP_004.Z = zincblende_Z(31,15);
    matZ.GaP_004.occupation = zincblende_occupation(1);
    matZ.GaP_004.H = [4,0,0];
    matZ.GaP_004.r = zincblende_r();

    matZ.AlP_002.xyspace = 5.4580e-10;
    matZ.AlP_002.poissonratio = 0.3330;
    matZ.AlP_002.zspace = matZ.AlP_002.xyspace/2;
    matZ.AlP_002.V = matZ.AlP_002.xyspace^3;
    matZ.AlP_002.Z = zincblende_Z(13,15);
    matZ.AlP_002.occupation = zincblende_occupation(1);
    matZ.AlP_002.H = [2,0,0];
    matZ.AlP_002.r = zincblende_r();

    matZ.AlP_004.xyspace = 5.4580e-10;
    matZ.AlP_004.poissonratio = 0.3330;
    matZ.AlP_004.zspace = matZ.AlP_004.xyspace/4;
    matZ.AlP_004.V = matZ.AlP_004.xyspace^3;
    matZ.AlP_004.Z = zincblende_Z(13,15);
    matZ.AlP_004.occupation = zincblende_occupation(1);
    matZ.AlP_004.H = [4,0,0];
    matZ.AlP_004.r = zincblende_r();

    matZ.InP_002.xyspace = 5.8687e-10;
    matZ.InP_002.poissonratio = 0.36;
    matZ.InP_002.zspace = matZ.InP_002.xyspace/2;
    matZ.InP_002.V = matZ.InP_002.xyspace^3;
    matZ.InP_002.Z = zincblende_Z(49,15);
    matZ.InP_002.occupation = zincblende_occupation(1);
    matZ.InP_002.H = [2,0,0];
    matZ.InP_002.r = zincblende_r();

    matZ.InP_004.xyspace = 5.8687e-10;
    matZ.InP_004.poissonratio = 0.36;
    matZ.InP_004.zspace = matZ.InP_004.xyspace/4;
    matZ.InP_004.V = matZ.InP_004.xyspace^3;
    matZ.InP_004.Z = zincblende_Z(49,15);
    matZ.InP_004.occupation = zincblende_occupation(1);
    matZ.InP_004.H = [4,0,0];
    matZ.InP_004.r = zincblende_r();

    matZ.AlAs_002.xyspace = 5.6618e-10;
    matZ.AlAs_002.poissonratio = 0.3240;
    matZ.AlAs_002.zspace = matZ.AlAs_002.xyspace/2;
    matZ.AlAs_002.V = matZ.AlAs_002.xyspace^3;
    matZ.AlAs_002.Z = zincblende_Z(13,33);
    matZ.AlAs_002.occupation = zincblende_occupation(1);
    matZ.AlAs_002.H = [2,0,0];
    matZ.AlAs_002.r = zincblende_r();

    matZ.AlAs_004.xyspace = 5.6618e-10;
    matZ.AlAs_004.poissonratio = 0.3240;
    matZ.AlAs_004.zspace = matZ.AlAs_004.xyspace/4;
    matZ.AlAs_004.V = matZ.AlAs_004.xyspace^3;
    matZ.AlAs_004.Z = zincblende_Z(13,33);
    matZ.AlAs_004.occupation = zincblende_occupation(1);
    matZ.AlAs_004.H = [4,0,0];
    matZ.AlAs_004.r = zincblende_r();

    matZ.InAs_002.xyspace = 6.0584e-10;
    matZ.InAs_002.poissonratio = 0.3520;
    matZ.InAs_002.zspace = matZ.InAs_002.xyspace/2;
    matZ.InAs_002.V = matZ.InAs_002.xyspace^3;
    matZ.InAs_002.occupation = zincblende_occupation(1);
    matZ.InAs_002.Z = zincblende_Z(49,33);
    matZ.InAs_002.H = [2,0,0];
    matZ.InAs_002.r = zincblende_r();

    matZ.InAs_004.xyspace = 6.0584e-10;
    matZ.InAs_004.poissonratio = 0.3520;
    matZ.InAs_004.zspace = matZ.InAs_004.xyspace/4;
    matZ.InAs_004.V = matZ.InAs_004.xyspace^3;
    matZ.InAs_004.occupation = zincblende_occupation(1);
    matZ.InAs_004.Z = zincblende_Z(49,33);
    matZ.InAs_004.H = [4,0,0];
    matZ.InAs_004.r = zincblende_r();

    matZ.InSb_002.xyspace = 6.479e-10;
    matZ.InSb_002.poissonratio = 0.35;
    matZ.InSb_002.zspace = matZ.InSb_002.xyspace/2;
    matZ.InSb_002.V = matZ.InSb_002.xyspace^3;
    matZ.InSb_002.occupation = zincblende_occupation(1);
    matZ.InSb_002.Z = zincblende_Z(49,51);
    matZ.InSb_002.H = [2,0,0];
    matZ.InSb_002.r = zincblende_r();

    matZ.InSb_004.xyspace = 6.479e-10;
    matZ.InSb_004.poissonratio = 0.35;
    matZ.InSb_004.zspace = matZ.InSb_004.xyspace/4;
    matZ.InSb_004.V = matZ.InSb_004.xyspace^3;
    matZ.InSb_004.occupation = zincblende_occupation(1);
    matZ.InSb_004.Z = zincblende_Z(49,51);
    matZ.InSb_004.H = [4,0,0];
    matZ.InSb_004.r = zincblende_r();

    matZ.AlSb_002.xyspace = 6.1355e-10;
    matZ.AlSb_002.poissonratio = 0.35;
    matZ.AlSb_002.zspace = matZ.AlSb_002.xyspace/2;
    matZ.AlSb_002.V = matZ.AlSb_002.xyspace^3;
    matZ.AlSb_002.occupation = zincblende_occupation(1);
    matZ.AlSb_002.Z = zincblende_Z(13,51);
    matZ.AlSb_002.H = [2,0,0];
    matZ.AlSb_002.r = zincblende_r();

    matZ.AlSb_004.xyspace = 6.1355e-10;
    matZ.AlSb_004.poissonratio = 0.35;
    matZ.AlSb_004.zspace = matZ.AlSb_004.xyspace/4;
    matZ.AlSb_004.V = matZ.AlSb_004.xyspace^3;
    matZ.AlSb_004.occupation = zincblende_occupation(1);
    matZ.AlSb_004.Z = zincblende_Z(13,51);
    matZ.AlSb_004.H = [4,0,0];
    matZ.AlSb_004.r = zincblende_r();

    matZ.GaN_cubic_002.xyspace = 4.5034e-10;
    matZ.GaN_cubic_002.poissonratio = 0.33;
    matZ.GaN_cubic_002.zspace = matZ.GaN_cubic_002.xyspace/2;
    matZ.GaN_cubic_002.V = matZ.GaN_cubic_002.xyspace^3;
    matZ.GaN_cubic_002.occupation = zincblende_occupation(1);
    matZ.GaN_cubic_002.Z = zincblende_Z(31,7);
    matZ.GaN_cubic_002.H = [2,0,0];
    matZ.GaN_cubic_002.r = zincblende_r();

    matZ.GaN_cubic_004.xyspace = 4.5034e-10;
    matZ.GaN_cubic_004.poissonratio = 0.33;
    matZ.GaN_cubic_004.zspace = matZ.GaN_cubic_004.xyspace/4;
    matZ.GaN_cubic_004.V = matZ.GaN_cubic_004.xyspace^3;
    matZ.GaN_cubic_004.occupation = zincblende_occupation(1);
    matZ.GaN_cubic_004.Z = zincblende_Z(31,7);
    matZ.GaN_cubic_004.H = [4,0,0];
    matZ.GaN_cubic_004.r = zincblende_r();

              % the only possible reflections are (222), (444), ...
    matZ.Al2O3.V = 84.89212148e-30;
    matZ.Al2O3.zspace = 12.9900000055889e-10/3/2;
    matZ.Al2O3.xyspace = 4.75799990823791e-10;
    matZ.Al2O3.poissonratio = 0.25;
    matZ.Al2O3.occupation=[1,1,1,1,1,1,1,1,1,1]';
    matZ.Al2O3.Z = [13,13,13,13,8,8,8,8,8,8]';
    matZ.Al2O3.H = [2,2,2];
    matZ.Al2O3.r = [.35228000   .35228000   .35228000
                   -.35228000  -.35228000  -.35228000
                    .14772000   .14772000   .14772000
                   -.14772000  -.14772000  -.14772000
                   -.05640000   .55640000   .25000000
                    .05640000  -.55640000  -.25000000
                    .55640000   .25000000  -.05640000
                   -.55640000  -.25000000   .05640000
                    .25000000  -.05640000   .55640000
                   -.25000000   .05640000  -.55640000];

    % second-order reflection
    matZ.Al2O3_second.V = 84.89212148e-30;
    matZ.Al2O3_second.zspace = 12.9900000055889e-10/3/4;
    matZ.Al2O3_second.xyspace = 4.75799990823791e-10;
    matZ.Al2O3_second.poissonratio = 0.25;
    matZ.Al2O3_second.occupation=[1,1,1,1,1,1,1,1,1,1]';
    matZ.Al2O3_second.Z = [13,13,13,13,8,8,8,8,8,8]';
    matZ.Al2O3_second.H = [4,4,4];
    matZ.Al2O3_second.r = [.35228000   .35228000   .35228000
                   -.35228000  -.35228000  -.35228000
                    .14772000   .14772000   .14772000
                   -.14772000  -.14772000  -.14772000
                   -.05640000   .55640000   .25000000
                    .05640000  -.55640000  -.25000000
                    .55640000   .25000000  -.05640000
                   -.55640000  -.25000000   .05640000
                    .25000000  -.05640000   .55640000
                   -.25000000   .05640000  -.55640000];

    matZ.AlN_0002.xyspace = 3.1106e-10;
    matZ.AlN_0002.zspace = 2.48975e-10;
    matZ.AlN_0002.V = 4.17258030417540e-29;
    matZ.AlN_0002.poissonratio = 0.203;
    matZ.AlN_0002.occupation = wurtzite_occupation(1);
    matZ.AlN_0002.Z = wurtzite_Z(13,7);
    matZ.AlN_0002.H = [0,0,2];
    matZ.AlN_0002.r = wurtzite_r();

    matZ.AlN_0004.xyspace = 3.1106e-10;
    matZ.AlN_0004.zspace = 2.48975e-10/2;
    matZ.AlN_0004.V = 4.17258030417540e-29;
    matZ.AlN_0004.poissonratio = 0.203;
    matZ.AlN_0004.occupation = wurtzite_occupation(1);
    matZ.AlN_0004.Z = wurtzite_Z(13,7);
    matZ.AlN_0004.H = [0,0,4];
    matZ.AlN_0004.r = wurtzite_r();

    matZ.GaN_0002.xyspace = 3.1896e-10;
    matZ.GaN_0002.zspace = 2.59275e-10;
    matZ.GaN_0002.V = 4.56871130048379e-29;
    matZ.GaN_0002.poissonratio = 0.183;
    matZ.GaN_0002.occupation = wurtzite_occupation(1);
    matZ.GaN_0002.Z = wurtzite_Z(31,7);
    matZ.GaN_0002.H = [0,0,2];
    matZ.GaN_0002.r = wurtzite_r();

    matZ.GaN_0004.xyspace = 3.1896e-10;
    matZ.GaN_0004.zspace = 2.59275e-10/2;
    matZ.GaN_0004.V = 4.56871130048379e-29;
    matZ.GaN_0004.poissonratio = 0.183;
    matZ.GaN_0004.occupation = wurtzite_occupation(1);
    matZ.GaN_0004.Z = wurtzite_Z(31,7);
    matZ.GaN_0004.H = [0,0,4];
    matZ.GaN_0004.r = wurtzite_r();

    matZ.InN_0002.xyspace = 3.5378e-10;
    matZ.InN_0002.zspace = 2.85165e-10;
    matZ.InN_0002.V = 6.18192032571193e-29;
    matZ.InN_0002.poissonratio = 0.272;
    matZ.InN_0002.occupation = wurtzite_occupation(1);
    matZ.InN_0002.Z = wurtzite_Z(49,7);
    matZ.InN_0002.H = [0,0,2];
    matZ.InN_0002.r = wurtzite_r();

    matZ.InN_0004.xyspace = 3.5378e-10;
    matZ.InN_0004.zspace = 2.85165e-10/2;
    matZ.InN_0004.V = 6.18192032571193e-29;
    matZ.InN_0004.poissonratio = 0.272;
    matZ.InN_0004.occupation = wurtzite_occupation(1);
    matZ.InN_0004.Z = wurtzite_Z(49,7);
    matZ.InN_0004.H = [0,0,4];
    matZ.InN_0004.r = wurtzite_r();
end
