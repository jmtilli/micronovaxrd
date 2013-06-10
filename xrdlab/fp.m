% Hönl corrections for Cu Kalpha (Henke et al, interpolated when Cu Kalpha not available)
% TODO: Java code to interpolate and give this to Matlab for arbitrary wavelengths

function y = fp(Z,lambda)
    global fpi_table;
    global fp_wavelength;
    if isempty(fp_wavelength) || isempty(fpi_table)
        fd = fopen('fp1_table.txt');
        fp1_table = fscanf(fd,'%g');
        fclose(fd);
        fd = fopen('fp2_table.txt');
        fp2_table = fscanf(fd,'%g');
        fclose(fd);
        fd = fopen('fp_wavelength.txt');
        fp_wavelength = fscanf(fd,'%g');
        fclose(fd);

        fpi_table = fp1_table + i*fp2_table;
    end
    if abs(lambda/fp_wavelength - 1) > 1e-4
        error(['Only for wavelength ',sprintf('%g',fp_wavelength)]);
    end
    y = reshape(fpi_table(Z),size(Z));
end
