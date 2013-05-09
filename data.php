<?php
/**
 * PHP functions to read the data.db file or the SQL data.
 * Note: Saves of VirtualPack versions before 2.0.0 are not supported.
 * 
 * @author Siguza
 */
namespace net\drgnome\virtualpack;

define('SEP_0', ':');
for($i = 1; $i < 5; $i++)
{
    define('SEP_'.$i, chr($i + 16));
}

/**
 * Read the data.db file
 * 
 * @param string $path The path to the data.db file
 * @return array An array containing all the data (see end of the file).
 */
function readFile($path)
{
    $array = array();
    foreach(explode("\n", file_get_contents($path)) as $line)
    {
        $split = explode(SEP_4, $line);
        if(count($split) <= 1)
        {
            continue;
        }
        $array[] = array('world' => $split[0], 'user' => $split[1], 'data' => $split[2]);
    }
    return readData($array);
}

/**
 * Read the VirtualPack SQL data
 *
 * @param array $rows An array containing all rows from the VirtualPack like array(array('world' => '*', 'user' => 'siguza', 'data' => '...'), ...)
 * @return array An array containing all the data (see end of the file).
 */
function readData($rows)
{
    $data = array();
    foreach($rows as $row)
    {
        if(!isset($data[$row['world']]))
        {
            $data[$row['world']] = array();
        }
        $current = &$data[$row['world']][$row['user']];
        $current = array('chests' => array(), 'furnaces' => array(), 'brewingstands' => array(), 'materializer' => array(), 'workbench' => FALSE, 'uncrafter' => FALSE, 'anvil' => FALSE, 'enchanttable' => FALSE, 'bookshelves' => 0, 'links' => array('furnace' => 0, 'brewingstand' => 0), 'left' => array(), 'msg' => array());
        foreach(explode(SEP_0, $row['data']) as $component)
        {
            $parts = explode(SEP_1, $component);
            $num = count($parts);
            if($num <= 1)
            {
                continue;
            }
            switch($parts[0])
            {
                case 'w':
                    $current['workbench'] = $parts[1] == '1';
                    break;
                case 'u':
                    $current['uncrafter'] = $parts[1] == '1';
                    break;
                case 'e':
                    $current['enchanttable'] = $parts[1] == '1';
                    $current['bookshelves'] = (int)$parts[2];
                    break;
                case 'av':
                    $current['anvil'] = $parts[1] == '1';
                    break;
                case 'm':
                    if($parts[1] != '1')
                    {
                        break;
                    }
                    $current['materializer']['value'] = (double)$parts[2];
                    $current['materializer']['all'] = FALSE;
                    $current['materializer']['unlocked'] = array();
                    for($i = 0; $i < $num; $i++)
                    {
                        $current['materializer']['unlocked'][] = 'MATERIAL('.implode(', ', explode(':', base64_decode($parts[$i]))).')';
                    }
                    break;
                case 'c':
                    $chest = &$current['chests'][];
                    $chest = array();
                    for($i = 1; $i < $num; $i++)
                    {
                        $chest[] = base64_decode($parts[$i]);
                    }
                    break;
                case 'f':
                    $current['furnaces'][] = array(base64_decode($parts[1]), base64_decode($parts[2]), base64_decode($parts[3]), (double)$parts[4], (int)$parts[5], (double)$parts[6], (int)$parts[7], (double)$parts[8]);
                    break;
                case 'b':
                    $current['brewingstands'][] = array(base64_decode($parts[1]), base64_decode($parts[2]), base64_decode($parts[3]), base64_decode($parts[4]), (double)$parts[5], (int)$parts[6]);
                    break;
                case 'fl':
                    $current['links']['furnace'] = (int)$parts[1];
                    break;
                case 'bl':
                    $current['links']['brewingstand'] = (int)$parts[1];
                    break;
                case 'lft':
                    for($i = 1; $i < $num; $i++)
                    {
                        $current['left'][] = base64_decode($parts[$i]);
                    }
                    break;
                case 'msg':
                    for($i = 1; $i < $num; $i++)
                    {
                        $current['msg'][] = base64_decode($parts[$i]);
                    }
                    break;
            }
        }
    }
    return $data;
}


/* The structure of the "data" array:
 * If a component doesn't exist (the materializer for example), it will just be an empty array.

array(
        'world' => array(
                'username' => array(
                        'chests' => array(
                                0 => array(
                                        0 => 'An ItemStack in NBT format',
                                        1 => 'an empty string for no item',
                                        2 => '...'
                                ),
                                1 => array('...'),
                                '...'
                        ),
                        'furnaces' => array(
                                0 => (NBT)'ingredient',
                                1 => (NBT)'fuel',
                                2 => (NBT)'result',
                                3 => (double)'burnTime',
                                4 => (int)'ticksForCurrentFuel (no idea what it is)',
                                5 => (double)'cookTime',
                                6 => (int)'link to chest (0 if not linked)',
                                7 => (double)'burn speed',
                        ),
                        'brewingstands' => array(
                                0 => (NBT)'slot 1',
                                1 => (NBT)'slot 2',
                                2 => (NBT)'slot 3',
                                3 => (NBT)'ingredient',
                                4 => (double)'brewTime',
                                5 => (int)'link'
                                ),
                        'materializer' => array(
                                'value' => (double)'stored matter currency',
                                'all' => (bool)'TRUE if unlocked via god item, FALSE otherwise',
                                'unlocked' => array(
                                        0 => 'MATERIAL(ID, META)',
                                        1 => '...',
                                        '...'
                                        )
                                ),
                        'workbench' => (bool)'TRUE or FALSE',
                        'uncrafter' => (bool)'TRUE or FALSE',
                        'anvil' => (bool)'TRUE or FALSE',
                        'enchanttable' => (bool)'TRUE or FALSE',
                        'bookshelves' => (int)'amount of bookshelves',
                        'links' => array(
                                'furnace' => (int)'amount of links',
                                'brewingstand' => (int)'...'
                                ),
                        'left' => array(
                                0 => (NBT)'a sent item that hasn\'t been delivered yet',
                                1 => '...',
                                '...'
                                ),
                        'msg' => array(
                                0 => 'Message from a sent item',
                                1 => '...',
                                '...'
                                )
                ),
                'siguza' => array('...'),
                '...'
                ),
        'world_nether' => '...',
        '...'
        );
*/