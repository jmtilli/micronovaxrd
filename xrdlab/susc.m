% r: matrix of row vectors of atomic positions
% Z: atomic numbers (column vector)
% V: unit cell volume
% H: miller index (row vector)
% zspace: spacing of reflecting planes (includes reflection order)
% lambda: wavelength







% Calculates electric susceptibility

% IMPORTANT NOTE: If you modify this, you must modify matsusc2_premixed!
function chih = susc(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, H, zspace, lambda)

    r_e = 2.817940325e-15;

    s = 1/(2*zspace);

    DW = exp(-B.*s^2);
    asf = sum(exp(-sf_b*s^2).*sf_a,2)+sf_c;
    f = (asf + hoenl).*DW;
    Fh = sum(occupation .* f.*exp(-2*pi*i*r*H'));
    chih = -r_e*lambda^2/(pi*V) * Fh;


    % DEBUG
    %disp('<debug>');
    %fr = real(f);
    %fi = imag(f);

    %Frh = sum(occupation .* fr.*exp(-2*pi*i*r*H'));
    %Fih = sum(occupation .* fi.*exp(-2*pi*i*r*H'));

    %chirh = -r_e*lambda^2/(pi*V) * Frh;
    %chiih = -r_e*lambda^2/(pi*V) * Fih;

    %chirha = abs(chirh)
    %chiiha = abs(chiih)
    %phasediff = (angle(chirh)-angle(chiih))/pi
    %disp('</debug>');

end






