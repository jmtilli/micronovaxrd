% Z must be a vector
% gets the scattering factor interpolating coefficients for Z.
%
% The coefficients are stored in a table sf_table.txt
% The file must contain the number of gaussians used in interpolation
% and the parameters starting from element 1. For example, a file which
% contains four gaussians will look like this:
% 
% 4
%
% a1 b1 a2 b2 a3 b3 a4 b4 c
% a1 b1 a2 b2 a3 b3 a4 b4 c
%
% In the file sf_table.txt, the units of b are Ų, a and c are unitless.
% However, this function returns sf_bvector in SI units.
%
function [sf_avector, sf_bvector, sf_cvector] = sf_vectors(Z)
    global sf_atable; % an NxM matrix
    global sf_btable; % an NxM matrix
    global sf_ctable; % an Nx1 vector

    if isempty(sf_atable) || isempty(sf_btable) || isempty(sf_ctable)
        fd = fopen('sf_table.txt');
        sf_rawtable = fscanf(fd,'%g');
        fclose(fd);

        n_gaussians = sf_rawtable(1);
        assert(ceil(n_gaussians)-n_gaussians == 0); % must be integer

        sf_datatable = sf_rawtable(2:end);

        n_elements = length(sf_datatable)/(2*n_gaussians+1);
        assert(ceil(n_elements)-n_elements == 0); % must be integer

        sf_table = reshape(sf_datatable, 2*n_gaussians+1, n_elements)';

        sf_atable = sf_table(:,1:2:end-1);
        sf_btable = sf_table(:,2:2:end-1)/1e20; % from Ų to m�
        sf_ctable = sf_table(:,end);
    end

    sf_avector = sf_atable(Z,:);
    sf_bvector = sf_btable(Z,:);
    sf_cvector = sf_ctable(Z,:);
end
