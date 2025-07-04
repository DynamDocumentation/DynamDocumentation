import React, { useState, useEffect } from 'react';
import { styled, useTheme } from '@mui/material/styles';
import { 
    Paper, Select, MenuItem, FormControl, InputLabel
} from '@mui/material';
import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import CssBaseline from '@mui/material/CssBaseline';
import MuiAppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import Typography from '@mui/material/Typography';
import { useNavigate } from 'react-router-dom';
import { Outlet } from 'react-router';
import axios from 'axios';

// Componentes customizados
import NamespaceAccordion from '../components/NamespaceAccordion';
import FileExplorer from '../components/FileExplorer';
import ToolsMenu from '../components/ToolsMenu';

const drawerWidth = 260;

const Main = styled('main', { shouldForwardProp: (prop) => prop !== 'open' })(
    ({ theme }) => ({
        flexGrow: 1,
        padding: theme.spacing(3),
        transition: theme.transitions.create('margin', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
        marginLeft: `-${drawerWidth}px`,
        variants: [
            {
                props: ({ open }) => open,
                style: {
                    transition: theme.transitions.create('margin', {
                        easing: theme.transitions.easing.easeOut,
                        duration: theme.transitions.duration.enteringScreen,
                    }),
                    marginLeft: 0,
                },
            },
        ],
    }),
);

const AppBar = styled(MuiAppBar, {
    shouldForwardProp: (prop) => prop !== 'open',
})(({ theme }) => ({
    transition: theme.transitions.create(['margin', 'width'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),

    variants: [
        {
        props: ({ open }) => open,
        style: {
            width: `calc(100% - ${drawerWidth}px)`,
            marginLeft: `${drawerWidth}px`,
            transition: theme.transitions.create(['margin', 'width'], {
                easing: theme.transitions.easing.easeOut,
                duration: theme.transitions.duration.enteringScreen,
            }),
        },
        },
    ],
}));

const DrawerHeader = styled('div')(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
    justifyContent: 'flex-end',
}));

export default function Layout() {
    const theme = useTheme();
    const navigate = useNavigate();
    
    // Estados
    const [open, setOpen] = useState(false);
    const [data, setData] = useState(null);
    const [selectedLibrary, setSelectedLibrary] = useState('sklearn');
    const [libraries] = useState(['sklearn', 'pandas', 'numpy']);
    const [toolsOpen, setToolsOpen] = useState(false);
    const [fileDialogOpen, setFileDialogOpen] = useState(false);
    const [selectedFolder, setSelectedFolder] = useState('');
    const [folderFiles, setFolderFiles] = useState([]);
    
    // Handlers
    const handleDrawerOpen = () => {
        setOpen(true);
    };

    const handleDrawerClose = () => {
        setOpen(false);
    };
    
    const handleLibraryChange = (event) => {
        setSelectedLibrary(event.target.value);
    };
    
    const handleToolsToggle = () => {
        setToolsOpen(!toolsOpen);
    };
    
    const navigateToLibraryInstaller = () => {
        navigate('/library-input');
        setToolsOpen(false);
    };
    
    const handleTestButtonClick = (folder) => {
        setSelectedFolder(folder);
        
        // Simular obtenção de arquivos de uma pasta (em produção, seria uma chamada de API)
        let files = [];
        if (folder === 'numpy') {
            files = [
                { name: 'numpy.json', size: '856 KB' },
                { name: 'random.json', size: '142 KB' },
                { name: 'testing.json', size: '75 KB' },
                { name: 'matlib.json', size: '121 KB' },
                { name: 'lib.json', size: '223 KB' },
                { name: 'tests.json', size: '1 KB' },
                { name: 'conftest.json', size: '1 KB' }
            ];
        } else if (folder === 'sklearn') {
            files = [
                { name: 'sklearn_cluster.json', size: '325 KB' },
                { name: 'sklearn_model_selection.json', size: '498 KB' },
                { name: 'sklearn_linear_model.json', size: '712 KB' },
                { name: 'sklearn_tree.json', size: '158 KB' },
                { name: 'sklearn_pipeline.json', size: '89 KB' },
                { name: 'sklearn_svm.json', size: '254 KB' },
                { name: 'sklearn_preprocessing.json', size: '387 KB' }
            ];
        }
        
        setFolderFiles(files);
        setFileDialogOpen(true);
    };
    
    const handleCloseDialog = () => {
        setFileDialogOpen(false);
    };

    // Efeitos
    useEffect(() => {
        if (selectedLibrary) {
            axios.get(`http://127.0.0.1:8080/library/${selectedLibrary}`).then((response) => {
                setData(response.data);
                console.log(response.data);
            }).catch((error) => {
                console.error("Error fetching library data:", error);
                setData(null);
            });
        }
    }, [selectedLibrary]);

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            <AppBar position="fixed" open={open}>
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={handleDrawerOpen}
                        edge="start"
                        sx={[
                        {
                            mr: 2,
                        },
                        open && { display: 'none' },
                        ]}
                        disabled={!selectedLibrary}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
                        DynamDocumentation
                    </Typography>
                    <FormControl variant="outlined" size="small" sx={{ minWidth: 120, mr: 1, bgcolor: 'rgba(255,255,255,0.15)', borderRadius: 1 }}>
                        <InputLabel id="library-select-label" sx={{ color: 'white' }}>Biblioteca</InputLabel>
                        <Select
                            labelId="library-select-label"
                            id="library-select"
                            value={selectedLibrary}
                            onChange={handleLibraryChange}
                            label="Library"
                            sx={{ color: 'white' }}
                        >
                            {libraries.map((lib) => (
                                <MenuItem key={lib} value={lib}>{lib}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Toolbar>
            </AppBar>
            <Drawer
                sx={{
                width: drawerWidth,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: drawerWidth,
                    boxSizing: 'border-box',
                    backgroundColor: (theme) => theme.palette.background.default,
                    color: (theme) => theme.palette.text.primary,
                },
                }}
                variant="persistent"
                anchor="left"
                open={open}
            >
                <DrawerHeader>
                <IconButton onClick={handleDrawerClose}>
                    {theme.direction === 'ltr' ? <ChevronLeftIcon /> : <ChevronRightIcon />}
                </IconButton>
                </DrawerHeader>
                <Divider />
                
                {/* Menu de ferramentas */}
                <ToolsMenu 
                    toolsOpen={toolsOpen}
                    onToggleTools={handleToolsToggle}
                    onNavigateToLibraryInstaller={navigateToLibraryInstaller}
                    onTestButtonClick={handleTestButtonClick}
                />
                
                <Divider />
                <NamespaceAccordion data={data} />
            </Drawer>
            <Main open={open}>
                <DrawerHeader />
                <Paper elevation={3}>
                    <Outlet />
                </Paper>
            </Main>
            
            {/* Dialog do explorador de arquivos */}
            <FileExplorer 
                open={fileDialogOpen}
                onClose={handleCloseDialog}
                folder={selectedFolder}
                files={folderFiles}
            />
        </Box>
    );
}
